package com.magora.app.usersList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.magora.app.datastore.LocalUserStorage
import com.magora.app.model.User
import com.magora.app.usersList.di.UserDetailsScreenProvider
import com.magora.realmpaginator.RealmPagedList
import com.magora.realmpaginator.RealmPagedListBuilder
import io.reactivex.disposables.Disposable
import org.koin.core.KoinComponent
import ru.terrakok.cicerone.Router

private const val INITIAL_PAGE_SIZE = 50
private const val PAGE_SIZE = 30
private const val PREFETCH_DISTANCE = 10

class VmUsersList(
    app: Application,
    private val dsFactory: UsersListDataSourceFactory,
    private val localStorage: LocalUserStorage,
    private val router: Router,
    private val screenProvider: UserDetailsScreenProvider
) : AndroidViewModel(app), KoinComponent {
    private val _userListLiveData = MediatorLiveData<UsersResult>()
    val userListLiveData: LiveData<UsersResult> get() = _userListLiveData
    private var userListDisposable: Disposable? = null

    val contentData: RealmPagedList<Int, User>
        get() {
            val config = RealmPagedList.Config.Builder()
                .setInitialLoadSizeHint(INITIAL_PAGE_SIZE)
                .setPageSize(PAGE_SIZE)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .build()

            return RealmPagedListBuilder(dsFactory, config)
                .setInitialLoadKey(0)
                .setRealmData(localStorage.getUsers().users)
                .build()
        }

    init {
        _userListLiveData.run {
            addSource(dsFactory.contentData) { value = it.toFeedContentViewState() }
            addSource(dsFactory.nextPageData) { value = it.toFeedPagingViewState() }
            addSource(dsFactory.refreshData) { value = it.toFeedPtrViewState() }
            addSource(dsFactory.updateData) { value = it.toFeedSilentUpdateViewState() }
        }
    }

    fun refreshData() {
        dsFactory.makePullToRefresh.value?.invalidate(key = 0)
    }

    fun retryAfterPaginationError() {
        dsFactory.makeRetryAfterError.value?.invoke()
    }

    fun onUserClick(userId: Int) {
        router.navigateTo(screenProvider.get(userId))
    }

    override fun onCleared() {
        super.onCleared()
        dsFactory.destroy()
        userListDisposable?.dispose()
    }
}