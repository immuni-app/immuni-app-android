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

package it.ministerodellasalute.immuni.ui.onboarding.fragments.viewpager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.models.Province
import it.ministerodellasalute.immuni.ui.onboarding.fragments.ViewPagerFragmentDirections
import kotlin.math.abs
import kotlinx.android.synthetic.main.onboarding_province_fragment.*
import kotlinx.android.synthetic.main.onboarding_province_fragment.appBar
import kotlinx.android.synthetic.main.onboarding_province_fragment.next
import kotlinx.android.synthetic.main.onboarding_province_fragment.pageTitle
import kotlinx.android.synthetic.main.onboarding_province_fragment.recyclerView
import kotlinx.android.synthetic.main.onboarding_province_fragment.toolbarSeparator
import kotlinx.android.synthetic.main.onboarding_province_fragment.toolbarTitle
import kotlinx.coroutines.flow.combine

class ProvinceFragment :
    ViewPagerBaseFragment(R.layout.onboarding_province_fragment), ProvinceClickListener {

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
            pageTitle?.alpha = 1 - ratio
            description?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
            knowMore?.alpha = 1 - ratio
        })

        val adapter = ProvinceListAdapter(this)
        recyclerView.adapter = adapter

        next.isEnabled = false

        next.setSafeOnClickListener {
            viewModel.onNextTap()
        }

        knowMore.setSafeOnClickListener {
            val action = ViewPagerFragmentDirections.actionRegionProvinceExplanation()
            findNavController().navigate(action)
        }

        navigationIcon.setSafeOnClickListener {
            viewModel.onPrevTap()
        }

        combine(viewModel.provinces, viewModel.province) { provinces, province ->
            Pair(provinces, province)
        }.asLiveData().observe(viewLifecycleOwner, Observer { (provinces, province) ->
            adapter.data = provinces
            adapter.selectedProvince = province
            validate(provinces, province)
        })
    }

    private fun validate(provinces: List<Province>, province: Province?) {
        if (provinces.isEmpty()) return
        next.isEnabled = province != null && province in provinces
    }

    override fun onClick(item: Province) {
        viewModel.onProvinceSelected(item)
    }
}
