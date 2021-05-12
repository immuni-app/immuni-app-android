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
