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
import java.util.*

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
class RealmPagedStorage<T> internal constructor(internal val realmData: OrderedRealmCollection<T>) : AbstractList<T>() {
    internal var positionOffset = 0
        private set

    internal val firstLoadedItem: T? get() = if (realmData.isValid && !realmData.isEmpty()) realmData.first() else null

    internal val lastLoadedItem: T? get() = if (realmData.isValid && !realmData.isEmpty()) realmData.last() else null

    override val size: Int get() = if (realmData.isValid) realmData.size else 0

    internal interface Callback {
        fun onPagePrepended(added: Int)
        fun onPageAppended(added: Int)
    }

    override fun get(index: Int): T? = if (realmData.isValid) realmData[index] else null

    internal fun prependPage(count: Int, callback: Callback) {
        positionOffset -= count
        callback.onPagePrepended(count)
    }

    internal fun appendPage(count: Int, callback: Callback) {
        callback.onPageAppended(count)
    }
}
