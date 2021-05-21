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

package it.ministerodellasalute.immuni.ui.greencertificate

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBar
import it.ministerodellasalute.immuni.ui.greencertificate.tabadapter.TabAdapter
import kotlinx.android.synthetic.main.green_certificate.*

class GreenCertificate : Fragment(R.layout.green_certificate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBar(
            resources.getColor(
                R.color.background_darker,
                null
            )
        )

        tabLayout.addTab(tabLayout.newTab())

        val adapter = TabAdapter(requireContext(), this@GreenCertificate, tabLayout.tabCount)
        viewpager.adapter = adapter
        viewpager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewpager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
            viewpager.setCurrentItem(tab.position, true)
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        navigationIcon.setOnClickListener {
            findNavController().popBackStack()
        }

        generate.setOnClickListener {
            val action = GreenCertificateDirections.actionGenerateGC()
            findNavController().navigate(action)
        }
    }
}
