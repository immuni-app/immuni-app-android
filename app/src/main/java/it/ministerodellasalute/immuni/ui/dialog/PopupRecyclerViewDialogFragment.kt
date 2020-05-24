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

package it.ministerodellasalute.immuni.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.view.animateHide
import it.ministerodellasalute.immuni.extensions.view.animateShow
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.setTint
import kotlinx.android.synthetic.main.how_it_works_dialog.*
import kotlinx.android.synthetic.main.popup_recycler_view_dialog.*

/**
 * Base class for all popup dialog with base scrolling behaviour.
 */
open class PopupRecyclerViewDialogFragment : BottomSheetDialogDarkFragment() {

    /**
     * Set the collapsing toolbar title.
     */
    fun setTitle(title: String) {
        toolbarTitle.text = title
    }

    /**
     * Set the collapsing toolbar background color.
     */
    fun setToolbarColor(@ColorRes color: Int) {
        toolbar.setTint(resources.getColor(color))
    }

    /**
     * Inflate the popup main content using a [ViewStub].
     */
    fun setContentLayout(@LayoutRes layout: Int) {
        // ViewStub
        popupContent.layoutResource = layout
        popupContent.inflate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.popup_recycler_view_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back.setSafeOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // show/hide toolbar separator on scroll
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateToolbarSeparator()
            }
        })

        updateToolbarSeparator()
    }

    private var hiding: Boolean? = null
    private fun updateToolbarSeparator() {
        val firstVisibile = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
        if (firstVisibile == -1) return

        if (firstVisibile == 0) {
            if (hiding == true) return
            toolbarSeparator.animateHide()
            toolbarTitle.animateHide()
            hiding = true
        } else {
            if (hiding == false) return
            toolbarSeparator.animateShow()
            toolbarTitle.animateShow()
            hiding = false
        }
    }
}
