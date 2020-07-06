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
import androidx.core.view.postDelayed
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.logic.user.models.Region
import it.ministerodellasalute.immuni.ui.dialog.ConfirmationDialogListener
import it.ministerodellasalute.immuni.ui.dialog.openConfirmationDialog
import it.ministerodellasalute.immuni.ui.onboarding.fragments.ViewPagerFragmentDirections
import kotlin.math.abs
import kotlinx.android.synthetic.main.faq_fragment.appBar
import kotlinx.android.synthetic.main.faq_fragment.toolbarSeparator
import kotlinx.android.synthetic.main.faq_fragment.toolbarTitle
import kotlinx.android.synthetic.main.onboarding_region_fragment.*

class RegionFragment :
    ViewPagerBaseFragment(R.layout.onboarding_region_fragment), RegionClickListener,
    ConfirmationDialogListener {

    lateinit var adapter: RegionListAdapter

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.setLightStatusBar(resources.getColor(R.color.background))
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
            knowMore?.alpha = 1 - ratio
        })

        adapter = RegionListAdapter(this)
        recyclerView.adapter = adapter

        next.isEnabled = false

        next.setOnClickListener(null)
        next.setSafeOnClickListener {
            viewModel.onRegionNextTap()
        }

        knowMore.setSafeOnClickListener {
            val action = ViewPagerFragmentDirections.actionRegionProvinceExplanation()
            findNavController().navigate(action)
        }

        adapter.data = viewModel.regions

        viewModel.region.asLiveData().observe(viewLifecycleOwner, Observer {
            adapter.selectedRegion = it
            adapter.notifyDataSetChanged()
            validate(it)
        })

        viewModel.askRegionConfirmation.observe(viewLifecycleOwner, Observer {
            openConfirmationDialog(
                positiveButton = getString(R.string.onboarding_region_abroad_alert_confirm),
                negativeButton = getString(R.string.onboarding_region_abroad_alert_cancel),
                message = getString(R.string.onboarding_region_abroad_alert_message),
                title = getString(R.string.onboarding_region_abroad_alert_title),
                cancelable = false,
                requestCode = REQUEST_CODE_ABROAD_CONFIRMATION
            )
        })
    }

    private fun validate(region: Region?) {
        next.isEnabled = region != null
    }

    override fun onClick(item: Region) {
        viewModel.onRegionSelected(item)
    }

    override fun onDialogPositive(requestCode: Int) {
        viewModel.onAbroadRegionConfirmed()
    }

    override fun onDialogNegative(requestCode: Int) {
        // Pass
    }

    companion object {
        const val REQUEST_CODE_ABROAD_CONFIRMATION = 291
    }
}
