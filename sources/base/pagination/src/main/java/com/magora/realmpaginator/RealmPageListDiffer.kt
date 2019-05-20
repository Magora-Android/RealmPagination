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
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults

/**
 * Developed by Magora Team (magora-systems.com). 02.06.18.
 */
class RealmPageListDiffer<T : RealmModel>(
    private val adapter: RecyclerView.Adapter<*>,
    private val pagedList: RealmPagedList<*, T>
) {
    val itemCount: Int get() = pagedList.size + additionalOffset
    var payloadProvider: ((Int) -> Any?)? = null
    var additionalOffset = 0

    init {
        when (val innerRealmData = pagedList.storage.realmData) {
            is RealmList -> innerRealmData.addChangeListener { _, changeSet -> onInternalDataChanged(changeSet) }
            is RealmResults -> innerRealmData.addChangeListener { _, changeSet -> onInternalDataChanged(changeSet) }
        }
    }

    private fun onInternalDataChanged(changeSet: OrderedCollectionChangeSet?) {
        if (changeSet != null) {
            if (changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                val insertions = changeSet.insertionRanges
                for (range in insertions) {
                    adapter.notifyItemRangeInserted(range.startIndex + additionalOffset, range.length)
                }
                return
            }
            // For deletions, the adapter has to be notified in reverse order.
            val deletions = changeSet.deletionRanges
            for (i in deletions.indices.reversed()) {
                val range = deletions[i]
                adapter.notifyItemRangeRemoved(range.startIndex + additionalOffset, range.length)
            }

            val insertions = changeSet.insertionRanges
            for (range in insertions) {
                adapter.notifyItemRangeInserted(range.startIndex + additionalOffset, range.length)
            }

            val modifications = changeSet.changeRanges
            for (range in modifications) {
                if (payloadProvider != null) {
                    for (index in 0 until range.length) {
                        adapter.notifyItemChanged(
                            range.startIndex + additionalOffset + index,
                            payloadProvider?.invoke(range.startIndex + additionalOffset + index)
                        )
                    }
                } else {
                    adapter.notifyItemRangeChanged(range.startIndex + additionalOffset, range.length, null)
                }
            }
        }
    }

    fun getItem(index: Int): T? {
        pagedList.loadAround(index - additionalOffset)
        return pagedList[index - additionalOffset]
    }
}
