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

import io.realm.OrderedRealmCollection
import io.realm.RealmModel

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
class RealmPagedListBuilder<Key, Value : RealmModel>(
    private val dataSourceFactory: RealmDataSource.Factory<Key, Value>,
    private val config: RealmPagedList.Config
) {
    private var realmData: OrderedRealmCollection<Value>? = null
    private var initialLoadKey: Key? = null
    private var boundaryCallback: RealmPagedList.BoundaryCallback<Value>? = null

    /**
     * First loading key passed to the first RealmPagedList/DataSource.
     *
     *
     * When a new PagedList/DataSource pair is created after the first, it acquires a load key from
     * the previous generation so that data is loaded around the position already being observed.
     *
     * @param key Initial load key passed to the first PagedList/DataSource.
     * @return this
     */
    fun setInitialLoadKey(key: Key?): RealmPagedListBuilder<Key, Value> = apply {
        initialLoadKey = key
    }

    /**
     * Sets a [RealmPagedList.BoundaryCallback] on each PagedList created, typically used to load
     * additional data from network when paging from local storage.
     *
     *
     * Pass a BoundaryCallback to listen to when the PagedList runs out of data to load. If this
     * method is not called, or `null` is passed, you will not be notified when each
     * DataSource runs out of data to provide to its PagedList.
     *
     *
     * If you are paging from a DataSource.Factory backed by local storage, you can set a
     * BoundaryCallback to know when there is no more information to page from local storage.
     * This is useful to page from the network when local storage is a cache of network data.
     *
     *
     * Note that when using a BoundaryCallback with a `LiveData<PagedList>`, method calls
     * on the callback may be dispatched multiple times - one for each PagedList/DataSource
     * pair. If loading network data from a BoundaryCallback, you should prevent multiple
     * dispatches of the same method from triggering multiple simultaneous network loads.
     *
     * @param boundaryCallback The boundary callback for listening to PagedList load state.
     * @return this
     */
    fun setBoundaryCallback(boundaryCallback: RealmPagedList.BoundaryCallback<Value>?): RealmPagedListBuilder<Key, Value> =
        apply {
            this.boundaryCallback = boundaryCallback
        }

    fun setRealmData(realmData: OrderedRealmCollection<Value>): RealmPagedListBuilder<Key, Value> = apply {
        this.realmData = realmData
    }

    fun build(): RealmPagedList<Key, Value> {
        checkNotNull(realmData) { "Realm data mustn't be null" }
        return create(realmData!!, initialLoadKey, config, boundaryCallback, dataSourceFactory)
    }

    private fun <Key, Value : RealmModel> create(
        realmData: OrderedRealmCollection<Value>,
        initialLoadKey: Key?,
        config: RealmPagedList.Config,
        boundaryCallback: RealmPagedList.BoundaryCallback<Value>?,
        dataSourceFactory: RealmDataSource.Factory<Key, Value>
    ): RealmPagedList<Key, Value> {
        return RealmPagedList.Builder(realmData, dataSourceFactory.create(), config)
            .setBoundaryCallback(boundaryCallback)
            .setInitialKey(initialLoadKey)
            .build()
    }
}
