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

package it.ministerodellasalute.immuni.ui.faq

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import kotlin.math.abs
import kotlinx.android.synthetic.main.faq_fragment.*
import org.koin.androidx.viewmodel.ext.android.getViewModel

class FaqFragment : Fragment(R.layout.faq_fragment), FaqClickListener {

    lateinit var viewModel: FaqViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background_darker))

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
        })

        viewModel = getViewModel()

        val adapter = FaqListAdapter(this)
        faqRecycler.adapter = adapter

        viewModel.questionAndAnswers.observe(viewLifecycleOwner) { adapter.data = it }

        navigationIcon.setSafeOnClickListener {
            // this fragment is accessible from the home as the root fragment
            // and from the settings as a middle fragment
            // so we handle both the back navigation cases
            if (!findNavController().popBackStack()) {
                activity?.finish()
            }
        }
    }

    override fun onClick(item: QuestionAndAnswer) {
        val action = FaqFragmentDirections.actionFaqDetailsDialogFragment(item)
        findNavController().navigate(action)
    }
}
