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

package it.ministerodellasalute.immuni.ui.suggestions

import android.view.View
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import kotlinx.android.synthetic.main.state_default_dialog.*

class StateDefaultDialogFragment : BaseStateDialogFragment(R.layout.state_default_dialog) {

    override val appBar: AppBarLayout
        get() = requireView().findViewById(R.id.appBar)
    override val backButton: View
        get() = navigationIcon
    override val scrollView: NestedScrollView
        get() = requireView().findViewById(R.id.scrollView)
    override val viewsToFadeInOnScroll: Array<View>
        get() = arrayOf(title, illustration)
    override val viewsToFadeOutOnScroll: Array<View>
        get() = arrayOf(toolbarTitle)
}
