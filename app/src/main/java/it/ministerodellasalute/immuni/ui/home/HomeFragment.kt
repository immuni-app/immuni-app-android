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

package it.ministerodellasalute.immuni.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.extensions.activity.setDarkStatusBarFullscreen
import it.ministerodellasalute.immuni.extensions.activity.setLightStatusBarFullscreen
import it.ministerodellasalute.immuni.extensions.utils.ScreenUtils
import it.ministerodellasalute.immuni.extensions.view.gone
import it.ministerodellasalute.immuni.extensions.view.setSafeOnClickListener
import it.ministerodellasalute.immuni.extensions.view.visible
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.ui.main.MainViewModel
import kotlinx.android.synthetic.main.home_fragment.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class HomeFragment : Fragment(),
    HomeClickListener {

    private lateinit var viewModel: MainViewModel

    // this value varies depending on device models
    // so will be overridden later
    private var statusBarHeight: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(R.color.statusBarLight))

        // hack to preserve the scrolling state across tab navigation
        // due to the top info card and list top padding that changes accordingly
        homeList.setPadding(
            homeList.paddingLeft,
            ScreenUtils.convertDpToPixels(requireContext(), 500), // big padding
            homeList.paddingRight,
            homeList.paddingBottom
        )
        // end hack

        with(homeList) {
            val settingsManager: ConfigurationSettingsManager by inject()
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = HomeListAdapter(
                requireContext(),
                this@HomeFragment,
                settingsManager
            )
        }

        scrollView.setOnApplyWindowInsetsListener { view, insets ->
            statusBarHeight = insets.systemWindowInsetTop
            insets
        }

        // Hide or show the status bar on scrolling
        scrollView.setOnScrollChangeListener { v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            updateScrollingEffect(scrollY)
        }

        infoCard.setSafeOnClickListener {
            when (viewModel.exposureStatus.value) {
                is ExposureStatus.None -> {
                }
                is ExposureStatus.Exposed -> {
                    val action =
                        HomeFragmentDirections.actionStateClose()
                    findNavController().navigate(action)
                }
                is ExposureStatus.Positive -> {
                    val action =
                        HomeFragmentDirections.actionStatePositive()
                    findNavController().navigate(action)
                }
            }
        }

        viewModel.homeListModel.observe(viewLifecycleOwner, Observer { newList ->
            (homeList.adapter as? HomeListAdapter)?.apply {
                update(newList)
            }
            updateInfoCard()
        })

        viewModel.exposureStatus.observe(viewLifecycleOwner, Observer {
            updateInfoCard()
        })
    }

    private fun updateInfoCard() {
        val exposureStatus = viewModel.exposureStatus.value ?: return

        // status bar theme light vs dark
        // and info card hide vs show
        when (exposureStatus) {
            is ExposureStatus.None -> {
                (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(
                    resources.getColor(
                        R.color.background
                    )
                )
                infoCard.gone()
            }
            else -> {
                (activity as? AppCompatActivity)?.setDarkStatusBarFullscreen(
                    resources.getColor(
                        R.color.statusBarLight
                    )
                )
                infoCard.visible()
            }
        }

        // change card info content
        val layout = when (exposureStatus) {
            is ExposureStatus.Exposed -> R.layout.info_card_close
            is ExposureStatus.Positive -> R.layout.info_card_positive
            else -> -1
        }

        // adapter recycler view top padding
        if (layout == -1) {
            homeList.setPadding(
                homeList.paddingLeft,
                0,
                homeList.paddingRight,
                homeList.paddingBottom
            )
        } else {
            val card: View = layoutInflater.inflate(layout, null)
            infoCard.findViewById<ViewGroup>(R.id.cardContainer)?.removeAllViews()
            infoCard.findViewById<ViewGroup>(R.id.cardContainer)?.addView(card)

            card.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    val cardHeight = bottom - top
                    val negativeMarginTop = ScreenUtils.convertDpToPixels(requireContext(), 20)
                    homeList.setPadding(
                        homeList.paddingLeft,
                        cardHeight - negativeMarginTop,
                        homeList.paddingRight,
                        homeList.paddingBottom
                    )
                    card.removeOnLayoutChangeListener(this)
                }
            })
        }
        updateScrollingEffect(scrollView.scrollY)
    }

    private fun updateScrollingEffect(scrollY: Int) {
        if (scrollY < 0) return

        val infoCardBottomPadding = ScreenUtils.convertDpToPixels(requireContext(), 32)
        val fadeThreshold = ScreenUtils.convertDpToPixels(requireContext(), 64)
        val collapsedHeight = statusBarHeight
        val infoCardHeight = infoCard.measuredHeight - infoCardBottomPadding

        // card translation up
        infoCard.translationY =
            -(scrollY.coerceAtMost((infoCardHeight - collapsedHeight).coerceAtLeast(0))).toFloat()

        // parallax effect
        infoCard.findViewById<ImageView>(R.id.infoCardAnimation)?.translationY =
            -scrollY.toFloat() * 0.2f
        infoCard.findViewById<TextView>(R.id.infoTitle)?.translationY = -scrollY.toFloat()
        infoCard.findViewById<TextView>(R.id.infoSubtitle)?.translationY =
            -scrollY.toFloat() * 0.5f

        // fade-out effect
        infoCard.findViewById<ImageView>(R.id.infoCardAnimation)?.alpha =
            1f - (scrollY.toFloat() / fadeThreshold).coerceIn(0f, 1f)
        infoCard.findViewById<TextView>(R.id.infoTitle)?.alpha =
            1f - (scrollY.toFloat() / fadeThreshold).coerceIn(0f, 1f)
        infoCard.findViewById<TextView>(R.id.infoSubtitle)?.alpha =
            1f - (scrollY.toFloat() / fadeThreshold).coerceIn(0f, 1f)
    }

    override fun onClick(item: HomeItemType, @IdRes viewId: Int) {
        when (item) {
            is ProtectionCard -> {
                if (viewId == R.id.reactivate) {
                    openOnboarding()
                } else if (viewId == R.id.knowMore) {
                    val action = HomeFragmentDirections.actionCheckAppStatus()
                    findNavController().navigate(action)
                }
            }
            is SectionHeader -> {
            }
            HowItWorksCard -> {
                openHowItWorks()
            }
            SelfCareCard -> {
                openSelfCare()
            }
            CountriesOfInterestCard -> {
                openCountriesOfInterest()
            }
            ReportPositivityCard -> {
                openReportPositivity()
            }
            GreenPassCard -> {
                openGreenPass()
            }
            is DisableExposureApi -> {
                openDisableExposureApi()
            }
        }
    }

    private fun openSelfCare() {
        val action =
            HomeFragmentDirections.actionStateDefault()
        findNavController().navigate(action)
    }

    private fun openOnboarding() {
        val action =
            HomeFragmentDirections.actionOnboardingActivity(false)
        findNavController().navigate(action)
    }

    private fun openHowItWorks() {
        val action =
            HomeFragmentDirections.actionHowitworks(
                true
            )
        findNavController().navigate(action)
    }

    private fun openDisableExposureApi() {
        val action = HomeFragmentDirections.actionDisableExposureApi()
        findNavController().navigate(action)
    }

    private fun openCountriesOfInterest() {
        val action = HomeFragmentDirections.actionCountriesOfInterest()
        findNavController().navigate(action)
    }

    private fun openReportPositivity() {
        val action = HomeFragmentDirections.actionDataUploadNav()
        findNavController().navigate(action)
    }

    private fun openGreenPass() {
        val action = HomeFragmentDirections.actionGreenCertificateNav()
        findNavController().navigate(action)
    }
}
