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
import androidx.annotation.MainThread
import io.realm.OrderedRealmCollection
import io.realm.RealmModel
import kotlin.math.max

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
class RealmContiguousPagedList<K, V : RealmModel> internal constructor(
    realmData: OrderedRealmCollection<V>,
    private val mDataSource: RealmContiguousDataSource<K, V>,
    boundaryCallback: BoundaryCallback<V>?,
    config: Config,
    key: K?,
    lastLoad: Int
) : RealmPagedList<K, V>(RealmPagedStorage<V>(realmData), boundaryCallback, config),
    RealmPagedStorage.Callback {
    private var mPrependWorkerRunning = false
    private var mAppendWorkerRunning = false
    private var mPrependItemsRequested = 0
    private var mAppendItemsRequested = 0

    override val dataSource: RealmDataSource<*, V> get() = mDataSource
    override val lastKey: Any? get() = mDataSource.getKey(lastLoad, lastItem)

    private val mReceiver = object : RealmPageResult.Receiver {
        @AnyThread
        override fun onPageResult(resultType: RealmPageResult.ResultType, pageResult: RealmPageResult) {
            if (pageResult.isInvalid) {
                detach()
                return
            }

            if (isDetached) {
                return
            }
            if (resultType == RealmPageResult.ResultType.APPEND) {
                storage.appendPage(pageResult.loadedCount, this@RealmContiguousPagedList)
            } else if (resultType == RealmPageResult.ResultType.PREPEND) {
                storage.prependPage(pageResult.loadedCount, this@RealmContiguousPagedList)
            }

            if (boundaryCallback != null) {
                val deferEmpty = storage.size == 0
                val deferBegin = !deferEmpty && resultType == RealmPageResult.ResultType.PREPEND && pageResult.loadedCount == 0
                val deferEnd = !deferEmpty && resultType == RealmPageResult.ResultType.APPEND && pageResult.loadedCount == 0
                deferBoundaryCallbacks(deferEmpty, deferBegin, deferEnd)
            }
        }
    }

    init {
        this.lastLoad = lastLoad
        if (mDataSource.isInvalid) {
            detach()
        } else {
            mDataSource.dispatchLoadInitial(key, config.initialLoadSizeHint, config.pageSize, mReceiver)
        }
        mDataSource.addInvalidatedCallback(this::invalidate)
    }

    override fun invalidate(key: K?) {
        mPrependItemsRequested = 0
        mAppendItemsRequested = 0
        mPrependWorkerRunning = false
        mAppendWorkerRunning = false
        mDataSource.dispatchLoadInitial(key, config.initialLoadSizeHint, config.pageSize, mReceiver)
    }

    @MainThread
    override fun loadAroundInternal(index: Int) {
        val prependItems = config.prefetchDistance - index
        val appendItems = index + config.prefetchDistance - storage.size

        mPrependItemsRequested = max(prependItems, mPrependItemsRequested)
        if (mPrependItemsRequested > 0) {
            schedulePrepend()
        }

        mAppendItemsRequested = max(appendItems, mAppendItemsRequested)
        if (mAppendItemsRequested > 0) {
            scheduleAppend()
        }
    }

    private fun schedulePrepend() {
        if (mPrependWorkerRunning) {
            return
        }
        mPrependWorkerRunning = true

        val position = storage.positionOffset

        // safe to access first item here - storage can't be empty if we're prepending
        val item = storage.firstLoadedItem
        if (isDetached) {
            return
        }
        if (mDataSource.isInvalid) {
            detach()
        } else {
            mDataSource.dispatchLoadBefore(position, storage.size, item!!, config.pageSize, mReceiver)
        }
    }

    private fun scheduleAppend() {
        if (mAppendWorkerRunning) {
            return
        }
        mAppendWorkerRunning = true

        val position = storage.size - 1 + storage.positionOffset

        // safe to access first item here - storage can't be empty if we're appending
        val item = storage.lastLoadedItem
        if (isDetached) {
            return
        }
        if (mDataSource.isInvalid) {
            detach()
        } else {
            mDataSource.dispatchLoadAfter(position, storage.size, item!!, config.pageSize, mReceiver)
        }
    }

    @MainThread
    override fun onPagePrepended(added: Int) {
        // consider whether to post more work, now that a page is fully prepended
        mPrependItemsRequested -= added
        mPrependWorkerRunning = false
        if (mPrependItemsRequested > 0) {
            // not done prepending, keep going
            schedulePrepend()
        }

        offsetBoundaryAccessIndices(added)
    }

    @MainThread
    override fun onPageAppended(added: Int) {
        // consider whether to post more work, now that a page is fully appended

        mAppendItemsRequested -= added
        mAppendWorkerRunning = false
        if (mAppendItemsRequested > 0) {
            // not done appending, keep going
            scheduleAppend()
        }
    }

    companion object {
        internal const val LAST_LOAD_UNSPECIFIED = -1
    }
}
