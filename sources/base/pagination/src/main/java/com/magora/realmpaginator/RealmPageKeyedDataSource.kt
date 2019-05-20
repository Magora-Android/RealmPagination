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

import androidx.annotation.GuardedBy
import androidx.annotation.UiThread
import io.realm.RealmModel

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
abstract class RealmPageKeyedDataSource<Key, Value : RealmModel> : RealmContiguousDataSource<Key, Value>() {
    @GuardedBy("keyLock")
    private var _nextKey: Key? = null

    @GuardedBy("keyLock")
    private var _previousKey: Key? = null

    private var previousKey: Key?
        get() = synchronized(keyLock) {
            return _previousKey
        }
        set(previousKey) = synchronized(keyLock) {
            _previousKey = previousKey
        }

    private var nextKey: Key?
        get() = synchronized(keyLock) {
            return _nextKey
        }
        set(nextKey) = synchronized(keyLock) {
            _nextKey = nextKey
        }

    private fun initKeys(previousKey: Key?, nextKey: Key?) {
        synchronized(keyLock) {
            _previousKey = previousKey
            _nextKey = nextKey
        }
    }

    protected fun invalidateKeys(previousKey: Key?, nextKey: Key?) {
        initKeys(previousKey, nextKey)
    }

    class LoadInitialParams<Key>(val initialKey: Key? = null, val requestedLoadSize: Int)

    class LoadParams<Key>(
        /**
         * Load items before/after this key.
         *
         * Returned data must begin directly adjacent to this position.
         */
        val key: Key,
        /**
         * Requested number of items to load.
         *
         * Returned page can be of this size, but it may be altered if that is easier, e.g. a
         * network data source where the backend defines page size.
         */
        val requestedLoadSize: Int,
        val currentItemsCount: Int
    )

    abstract class LoadInitialCallback<Key, Value> {

        abstract fun onResult(loadedCount: Int, previousPageKey: Key?, nextPageKey: Key?)
    }

    /**
     * Callback for RealmDataSource [.loadBefore] and
     * [.loadAfter] to return data.
     *
     *
     * A callback can be called only once, and will throw if called again.
     *
     *
     * It is always valid for a DataSource loading method that takes a callback to stash the
     * callback and call it later. This enables DataSources to be fully asynchronous, and to handle
     * temporary, recoverable error states (such as a network error that can be retried).
     *
     * @param <Key>   Type of data used to query pages.
     * @param <Value> Type of items being loaded.
    </Value></Key> */
    abstract class LoadCallback<Key, Value> {

        /**
         * Called to pass loaded data from a DataSource.
         *
         *
         * Call this method from your RealmDataSource's
         * [.loadBefore] and
         * [.loadAfter] methods to return data.
         *
         *
         * It is always valid to pass a different amount of data than what is requested. Pass an
         * empty list if there is no more data to load.
         *
         *
         * Pass the key for the subsequent page to load to adjacentPageKey. For example, if you've
         * loaded a page in [.loadBefore], pass the key for the
         * previous page, or `null` if the loaded page is the first. If in
         * [.loadAfter], pass the key for the next page, or
         * `null` if the loaded page is the last.
         *
         * @param loadedCount     List of items loaded from the PageKeyedDataSource.
         * @param adjacentPageKey Key for subsequent page load (previous page in [.loadBefore]
         * / next page in [.loadAfter]), or `null` if there are
         * no more pages to load in the current load direction.
         */
        @UiThread
        abstract fun onResult(loadedCount: Int, adjacentPageKey: Key?)
    }

    internal class LoadInitialCallbackImpl<Key, Value : RealmModel>(
        private val dataSource: RealmPageKeyedDataSource<Key, Value>,
        receiver: RealmPageResult.Receiver
    ) : LoadInitialCallback<Key, Value>() {
        private val callbackHelper = LoadCallbackHelper(
            dataSource,
            RealmPageResult.ResultType.INIT,
            receiver
        )

        override fun onResult(loadedCount: Int, previousPageKey: Key?, nextPageKey: Key?) {
            if (!callbackHelper.dispatchInvalidResultIfInvalid()) {
                dataSource.initKeys(previousPageKey, nextPageKey)
                callbackHelper.dispatchResultToReceiver(RealmPageResult(loadedCount))
            }
        }
    }

    internal class LoadCallbackImpl<Key, Value : RealmModel>(
        private val dataSource: RealmPageKeyedDataSource<Key, Value>,
        type: RealmPageResult.ResultType,
        receiver: RealmPageResult.Receiver
    ) : LoadCallback<Key, Value>() {
        private val callbackHelper = LoadCallbackHelper(dataSource, type, receiver)

        override fun onResult(loadedCount: Int, adjacentPageKey: Key?) {
            if (!callbackHelper.dispatchInvalidResultIfInvalid()) {
                if (callbackHelper.mResultType == RealmPageResult.ResultType.APPEND) {
                    dataSource.nextKey = adjacentPageKey
                } else {
                    dataSource.previousKey = adjacentPageKey
                }
                callbackHelper.dispatchResultToReceiver(RealmPageResult(loadedCount))
            }
        }
    }

    override fun getKey(position: Int, item: Value?): Key? {
        // don't attempt to persist keys, since we currently don't pass them to initial load
        return null
    }

    override fun dispatchLoadInitial(
        key: Key?,
        initialLoadSize: Int,
        pageSize: Int,
        receiver: RealmPageResult.Receiver
    ) {
        loadInitial(
            LoadInitialParams(key, initialLoadSize),
            LoadInitialCallbackImpl(this, receiver)
        )
    }


    override fun dispatchLoadAfter(
        currentEndIndex: Int,
        currentItemsCount: Int,
        currentEndItem: Value,
        pageSize: Int,
        receiver: RealmPageResult.Receiver
    ) {
        val key = nextKey
        if (key != null) {
            loadAfter(
                LoadParams(key, pageSize, currentItemsCount),
                LoadCallbackImpl(
                    this,
                    RealmPageResult.ResultType.APPEND,
                    receiver
                )
            )
        }
    }

    override fun dispatchLoadBefore(
        currentBeginIndex: Int,
        currentItemsCount: Int,
        currentBeginItem: Value,
        pageSize: Int,
        receiver: RealmPageResult.Receiver
    ) {
        val key = previousKey
        if (key != null) {
            loadBefore(
                LoadParams(key, pageSize, currentItemsCount),
                LoadCallbackImpl(
                    this,
                    RealmPageResult.ResultType.PREPEND,
                    receiver
                )
            )
        }
    }

    /**
     * Load initial data.
     *
     *
     * This method is called first to initialize a PagedList with data. If it's possible to count
     * the items that can be loaded by the DataSource, it's recommended to pass the loaded data to
     * the callback via the three-parameter
     * [RealmPageKeyedDataSource.LoadInitialCallback.onResult]. This enables PagedLists
     * presenting data from this source to display placeholders to represent unloaded items.
     *
     *
     * [RealmPageKeyedDataSource.LoadInitialParams.requestedLoadSize] is a hint, not a requirement, so it may be may be
     * altered or ignored.
     *
     * @param params   Parameters for initial load, including requested load size.
     * @param callback Callback that receives initial load data.
     */
    abstract fun loadInitial(params: LoadInitialParams<Key>, callback: LoadInitialCallback<Key, Value>)

    /**
     * Prepend page with the key specified by [LoadParams.key][RealmPageKeyedDataSource.LoadParams.key].
     *
     *
     * It's valid to return a different list size than the page size if it's easier, e.g. if your
     * backend defines page sizes. It is generally safer to increase the number loaded than reduce.
     *
     *
     * Data may be passed synchronously during the load method, or deferred and called at a
     * later time. Further loads going down will be blocked until the callback is called.
     *
     *
     * If data cannot be loaded (for example, if the request is invalid, or the data would be stale
     * and inconsistent, it is valid to call [.invalidate] to invalidate the data source,
     * and prevent further loading.
     *
     * @param params   Parameters for the load, including the key for the new page, and requested load
     * size.
     * @param callback Callback that receives loaded data.
     */
    abstract fun loadBefore(params: LoadParams<Key>, callback: LoadCallback<Key, Value>)

    /**
     * Append page with the key specified by [LoadParams.key][RealmPageKeyedDataSource.LoadParams.key].
     *
     *
     * It's valid to return a different list size than the page size if it's easier, e.g. if your
     * backend defines page sizes. It is generally safer to increase the number loaded than reduce.
     *
     *
     * Data may be passed synchronously during the load method, or deferred and called at a
     * later time. Further loads going down will be blocked until the callback is called.
     *
     *
     * If data cannot be loaded (for example, if the request is invalid, or the data would be stale
     * and inconsistent, it is valid to call [.invalidate] to invalidate the data source,
     * and prevent further loading.
     *
     * @param params   Parameters for the load, including the key for the new page, and requested load
     * size.
     * @param callback Callback that receives loaded data.
     */
    abstract fun loadAfter(params: LoadParams<Key>, callback: LoadCallback<Key, Value>)


    companion object {
        private val keyLock = Any()
    }
}
