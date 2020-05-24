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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.ui.dialog.BottomSheetDialogDarkFragment
import kotlin.math.abs

/**
 * Suggestions dialogs look very similar, they have different layouts, but have same behavior.
 * Therefore this abstract class provides correct CollapsingToolbar & BottomSheet behavior
 * and support for hiding/showing views on scroll.
 */
abstract class BaseStateDialogFragment(@LayoutRes private val layoutResId: Int) :
    BottomSheetDialogDarkFragment() {

    protected abstract val appBar: AppBarLayout
    protected abstract val backButton: View
    protected abstract val scrollView: NestedScrollView
    protected abstract val viewsToFadeInOnScroll: Array<View>
    protected abstract val viewsToFadeOutOnScroll: Array<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior?.let {
            onBottomSheetStateChanged(it.state)
            it.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    onBottomSheetStateChanged(newState)
                }
            })
        }

        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            enableBottomSheetScroll(verticalOffset == 0)

            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            viewsToFadeInOnScroll.forEach { it.alpha = 1 - ratio }
            viewsToFadeOutOnScroll.forEach { it.alpha = ratio }
        })

        backButton.setSafeOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun enableBottomSheetScroll(enable: Boolean) {
        bottomSheetBehavior?.isDraggable = enable
    }

    private fun enableContentScroll(enable: Boolean) {
        appBar.isNestedScrollingEnabled = enable
        scrollView.isNestedScrollingEnabled = enable
    }

    private fun onBottomSheetStateChanged(state: Int) {
        when (state) {
            BottomSheetBehavior.STATE_EXPANDED -> enableContentScroll(true)
            else -> enableContentScroll(false)
        }
    }
}
