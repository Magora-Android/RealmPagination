package android.arch.lifecycle

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 * <p>
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 * <p>
 * Note that only one observer is going to be notified of changes.
 */
private const val TAG = "SingleLiveEvent"

open class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val mPending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
        super.observe(owner, Observer<T> {
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        })
    }

    private var foreverObserver: Observer<in T>? = null
    private var innerForeverObserver: Observer<in T>? = null

    override fun observeForever(observer: Observer<in T>) {
        foreverObserver = observer
        innerForeverObserver = Observer {
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
        super.observeForever(innerForeverObserver!!)
    }

    override fun removeObserver(observer: Observer<in T>) {
        super.removeObserver(observer)
        if (observer == foreverObserver && innerForeverObserver != null) {
            removeObserver(innerForeverObserver!!)
            foreverObserver = null
            innerForeverObserver = null
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    override fun postValue(value: T) {
        mPending.set(true)
        super.postValue(value)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }
}
