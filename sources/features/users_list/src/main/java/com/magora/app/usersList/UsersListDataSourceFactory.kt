package com.magora.app.usersList

import android.arch.lifecycle.SingleLiveEvent
import android.arch.paging.PagingRequestHelper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magora.app.datastore.UserDataStorage
import com.magora.app.model.User
import com.magora.app.networking.NetworkState
import com.magora.realmpaginator.RealmDataSource
import com.magora.realmpaginator.RealmPageKeyedDataSource
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

private const val UPDATE_INTERVAL_MIN = 5L

class UsersListDataSourceFactory(
    private val getUsersUseCase: GetUserListUseCase,
    private val localStorage: UserDataStorage
) : RealmDataSource.Factory<Int, User>() {
    val contentData = SingleLiveEvent<NetworkState>()
    val refreshData = SingleLiveEvent<NetworkState>()
    val nextPageData = SingleLiveEvent<NetworkState>()
    val updateData = SingleLiveEvent<NetworkState>()
    val makePullToRefresh: LiveData<RealmDataSource<Int, User>> = MutableLiveData<RealmDataSource<Int, User>>()
    val makeRetryAfterError: LiveData<() -> Boolean> = MutableLiveData<() -> Boolean>()

    private val sJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + sJob)
    private val requestHelper = PagingRequestHelper()

    override fun create(): RealmDataSource<Int, User> {
        val result = object : RealmPageKeyedDataSource<Int, User>() {

            fun liveData() = if (isInvalidating) refreshData else contentData

            override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, User>) {
                val localCached = localStorage.getUsers()
                fun shouldUpdate(lastUpdateTime: Long) =
                    System.currentTimeMillis() - lastUpdateTime > TimeUnit.MINUTES.toMillis(UPDATE_INTERVAL_MIN)
                if (isInvalidating || shouldUpdate(localCached.lastUpdateTimestamp)) {
                    val request = PagingRequestHelper.Request { helperCallback ->
                        fun doOnLoadingStarted() {
                            liveData().value = NetworkState.Loading
                        }

                        fun onUsersLoaded(users: List<User>) {
                            localStorage.putUsers(users)
                            callback.onResult(users.size, null, nextPageKey = params.requestedLoadSize)
                            liveData().value = NetworkState.Success
                            helperCallback.recordSuccess()
                        }

                        fun onLoadUsersError(error: Throwable) {
                            liveData().value = NetworkState.Error(error)
                            helperCallback.recordFailure(error)
                        }

                        val tryToLoadData = localCached.users.isEmpty()
                        if (tryToLoadData) {
                            scope.launch {
                                doOnLoadingStarted()
                                try {
                                    val users =
                                        getUsersUseCase(page = params.initialKey ?: 0, pageSize = params.requestedLoadSize)
                                    onUsersLoaded(users)
                                } catch (e: Throwable) {
                                    onLoadUsersError(e)
                                } finally {
                                    isInvalidating = false
                                }
                            }
                        } else {
                            scope.launch {
                                doOnLoadingStarted()
                                try {
                                    val users =
                                        getUsersUseCase(page = params.initialKey ?: 0, pageSize = params.requestedLoadSize)
                                    onUsersLoaded(users)
                                } catch (e: Throwable) {
                                    onLoadUsersError(e)
                                } finally {
                                    isInvalidating = false
                                }
                            }
                        }
                    }
                    if (isInvalidating) {
                        requestHelper.recordResult(
                            PagingRequestHelper.RequestWrapper(
                                request,
                                requestHelper,
                                PagingRequestHelper.RequestType.INITIAL
                            ), null
                        )
                    }
                    requestHelper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL, request)
                } else {
                    contentData.value = NetworkState.Success
                    callback.onResult(localCached.users.size, null, nextPageKey = params.requestedLoadSize)
                    requestHelper.recordResult(
                        PagingRequestHelper.RequestWrapper(
                            emptyRequest,
                            requestHelper,
                            PagingRequestHelper.RequestType.INITIAL
                        ), null
                    )
                }
            }

            override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
                callback.onResult(0, null)
            }

            override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
                requestHelper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) { helperCallback ->
                    scope.launch {
                        try {
                            nextPageData.value = NetworkState.Loading

                            val users = getUsersUseCase(page = params.key, pageSize = params.requestedLoadSize)
                            localStorage.putUsersNextPage(users)

                            callback.onResult(users.size, params.key + params.requestedLoadSize)
                            nextPageData.value = NetworkState.Success
                            helperCallback.recordSuccess()
                        } catch (e: Throwable) {
                            nextPageData.value = NetworkState.Error(e)
                            helperCallback.recordFailure(e)
                        }
                    }
                }
            }
        }

        (makePullToRefresh as MutableLiveData<RealmDataSource<Int, User>>).value = result
        (makeRetryAfterError as MutableLiveData<() -> Boolean>).value = requestHelper::retryAllFailed
        return result
    }

    override fun destroy() {
        scope.coroutineContext.cancelChildren()
    }

    companion object {
        private val emptyRequest = PagingRequestHelper.Request { }
    }
}