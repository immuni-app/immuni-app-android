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
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.models.Region
import kotlin.math.abs
import kotlinx.android.synthetic.main.faq_fragment.appBar
import kotlinx.android.synthetic.main.faq_fragment.toolbarSeparator
import kotlinx.android.synthetic.main.faq_fragment.toolbarTitle
import kotlinx.android.synthetic.main.onboarding_region_fragment.*

class RegionFragment :
    ViewPagerBaseFragment(R.layout.onboarding_region_fragment), RegionClickListener {

    lateinit var adapter: RegionListAdapter

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.setLightStatusBar(ContextCompat.getColor(requireContext(), R.color.background))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // There is a bug in collapsing toolbarLayout which causes incorrect recyclerview sizing.
        // This can be removed after the issue is fixed
        // Issue: https://github.com/material-components/material-components-android/issues/1019
        view.postDelayed(0) { view.requestLayout() }

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            toolbarSeparator?.alpha = ratio
            pageTitle?.alpha = 1 - ratio
            description?.alpha = 1 - ratio
            toolbarTitle?.alpha = ratio
        })

        adapter = RegionListAdapter(this)
        recyclerView.adapter = adapter

        next.isEnabled = false

        next.setOnClickListener(null)
        next.setSafeOnClickListener {
            viewModel.onRegionNextTap()
        }

        adapter.data = viewModel.regions

        viewModel.region.asLiveData().observe(viewLifecycleOwner, Observer {
            adapter.selectedRegion = it
            adapter.notifyDataSetChanged()
            validate(it)
        })
    }

    private fun validate(region: Region?) {
        next.isEnabled = region != null
    }

    override fun onClick(item: Region) {
        viewModel.onRegionSelected(item)
    }
}
