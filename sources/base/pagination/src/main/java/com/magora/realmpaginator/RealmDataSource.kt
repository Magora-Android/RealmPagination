/*
    Realm pagination library.
    Copyright (C) 2019  Magora Systems.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.magora.realmpaginator

import androidx.annotation.AnyThread
import androidx.annotation.NonNull
import io.realm.RealmModel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
// Since we currently rely on implementation details of two implementations,
// prevent external subclassing, except through exposed subclasses
abstract class RealmDataSource<Key, Value : RealmModel> internal constructor() {
    private val _isInvalid = AtomicBoolean(false)
    val isInvalid: Boolean get() = _isInvalid.get()

    abstract class Factory<Key, Value : RealmModel> {
        abstract fun create(): RealmDataSource<Key, Value>

        open fun destroy() {

        }
    }

    internal class LoadCallbackHelper(
        private val mDataSource: RealmDataSource<*, *>,
        val mResultType: RealmPageResult.ResultType,
        private val mReceiver: RealmPageResult.Receiver
    ) {

        /**
         * Call before verifying args, or dispatching actual results
         *
         * @return true if DataSource was invalid, and invalid result dispatched
         */
        fun dispatchInvalidResultIfInvalid(): Boolean =
            if (mDataSource.isInvalid) {
                dispatchResultToReceiver(RealmPageResult.invalidResult)
                true
            } else false

        fun dispatchResultToReceiver(result: RealmPageResult) {
            mReceiver.onPageResult(mResultType, result)
        }
    }

    private val onInvalidatedCallbacks = CopyOnWriteArrayList<((Key?) -> Unit)>()

    /**
     * Add a callback to invoke when the DataSource is first invalidated.
     *
     * Once invalidated, a data source will not become valid again.
     *
     * A data source will only invoke its callbacks once - the first time [.invalidate]
     * is called, on that thread.
     *
     * @param onInvalidatedCallback The callback, will be invoked on thread that
     * [.invalidate] is called on.
     */
    @AnyThread
    fun addInvalidatedCallback(@NonNull onInvalidatedCallback: ((Key?) -> Unit)) {
        onInvalidatedCallbacks.add(onInvalidatedCallback)
    }

    /**
     * Remove a previously added invalidate callback.
     *
     * @param onInvalidatedCallback The previously added callback.
     */
    @AnyThread
    fun removeInvalidatedCallback(@NonNull onInvalidatedCallback: ((Key?) -> Unit)) {
        onInvalidatedCallbacks.remove(onInvalidatedCallback)
    }

    /**
     * Signal the data source to stop loading, and notify its callback.
     *
     * If invalidate has already been called, this method does nothing.
     */
    @AnyThread
    fun invalidate(key: Key? = null) {
        isInvalidating = true
        onInvalidatedCallbacks.forEach { it.invoke(key) }
    }

    @Volatile
    protected var isInvalidating = false
}
