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

import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmModel

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
abstract class AbstractRealmPagedListAdapter<T : RealmModel, VH : RecyclerView.ViewHolder>(
    pagedList: RealmPagedList<*, T>
) : RecyclerView.Adapter<VH>() {
    private val differ: RealmPageListDiffer<T> = RealmPageListDiffer(this, pagedList)

    protected fun setPayloadProvider(provider: ((Int) -> Any?)?) {
        differ.payloadProvider = provider
    }

    protected fun setAdditionalOffset(offset: Int) {
        differ.additionalOffset = offset
    }

    protected open fun getItem(position: Int): T? = differ.getItem(position)

    override fun getItemCount(): Int = differ.itemCount
}
