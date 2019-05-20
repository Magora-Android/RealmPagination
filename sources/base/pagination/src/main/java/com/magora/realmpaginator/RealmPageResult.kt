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

/**
 * Developed by Magora Team (magora-systems.com). 03.06.18.
 */
class RealmPageResult internal constructor(val loadedCount: Int) {
    val isInvalid: Boolean get() = this === invalidResult

    enum class ResultType {
        INIT,
        APPEND,
        PREPEND
    }

    internal interface Receiver {
        fun onPageResult(resultType: ResultType, pageResult: RealmPageResult)
    }

    companion object {
        internal val invalidResult = RealmPageResult(-1)
    }
}
