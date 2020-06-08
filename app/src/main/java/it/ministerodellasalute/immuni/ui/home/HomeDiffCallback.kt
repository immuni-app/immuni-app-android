/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.home

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil

class HomeDiffCallback(
    private val oldList: List<HomeItemType>,
    private val newList: List<HomeItemType>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        if (old::class != new::class) return false
        if (old is ProtectionCard) return true
        if (old is DisableExposureApi) return true
        if (old is SectionHeader && new is SectionHeader) {
            return old.title == new.title
        } else return old == new
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return false
    }

    @Nullable
    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return Any()
    }
}
