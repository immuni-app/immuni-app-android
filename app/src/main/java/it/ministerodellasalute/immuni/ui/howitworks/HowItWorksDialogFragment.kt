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

package it.ministerodellasalute.immuni.ui.howitworks

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.ui.dialog.PopupRecyclerViewDialogFragment
import kotlinx.android.synthetic.main.how_it_works_dialog.*

class HowItWorksDialogFragment : PopupRecyclerViewDialogFragment(),
    HowItWorksClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentLayout(R.layout.how_it_works_dialog)

        val args = navArgs<HowItWorksDialogFragmentArgs>()

        val adapter = HowItWorksListAdapter(requireContext(), this, args.value.showFaq)
        recyclerView.adapter = adapter

        setTitle(getString(R.string.permission_tutorial_how_immuni_works_title))

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        adapter.onIdle()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy != 0) {
                    adapter.onScrolling()
                }
            }
        })
    }

    override fun onClick(item: HowItWorksItem) {
        when (item) {
            is HowItWorksItem.Footer -> {
                openFaq()
            }
        }
    }

    private fun openFaq() {
        val action = HowItWorksDialogFragmentDirections.actionFaqActivity()
        findNavController().navigate(action)
        // close the previous dialog
        // important keep here, after navigating
        findNavController().popBackStack()
    }
}
