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

package it.ministerodellasalute.immuni.ui.greencertificate.tabadapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.ministerodellasalute.immuni.R

class TabAdapter(
    val context: Context,
    fm: Fragment,
    var totalTabs: Int
) :
    FragmentStateAdapter(fm) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TabActive()
            else -> createFragment(position)
        }
    }

    override fun getItemCount(): Int {
        return totalTabs
    }

    fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.green_pass_active)
            else -> getPageTitle(position)
        }
    }
}
