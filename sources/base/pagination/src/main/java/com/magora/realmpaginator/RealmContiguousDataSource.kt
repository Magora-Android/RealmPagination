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

import io.realm.RealmModel

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
abstract class RealmContiguousDataSource<Key, Value : RealmModel> : RealmDataSource<Key, Value>() {

    internal abstract fun dispatchLoadInitial(
        key: Key?,
        initialLoadSize: Int,
        pageSize: Int,
        receiver: RealmPageResult.Receiver
    )

    internal abstract fun dispatchLoadAfter(
        currentEndIndex: Int,
        currentItemsCount: Int,
        currentEndItem: Value,
        pageSize: Int,
        receiver: RealmPageResult.Receiver
    )

    internal abstract fun dispatchLoadBefore(
        currentBeginIndex: Int,
        currentItemsCount: Int,
        currentBeginItem: Value,
        pageSize: Int,
        receiver: RealmPageResult.Receiver
    )

    /**
     * Get the key from either the position, or item, or null if position/item invalid.
     *
     * Position may not match passed item's position - if trying to query the key from a position
     * that isn't yet loaded, a fallback item (last loaded item accessed) will be passed.
     */
    internal abstract fun getKey(position: Int, item: Value?): Key?
}
