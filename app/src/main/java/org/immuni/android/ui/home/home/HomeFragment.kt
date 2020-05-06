package org.immuni.android.ui.home.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.immuni.android.extensions.view.gone
import org.immuni.android.extensions.activity.setDarkStatusBarFullscreen
import org.immuni.android.extensions.activity.setLightStatusBarFullscreen
import org.immuni.android.extensions.view.visible
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.home_blocking_card.*
import kotlinx.android.synthetic.main.home_fragment.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.extensions.activity.disableDragging
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.ExposureNotificationManager
import org.immuni.android.models.survey.backgroundColor
import org.immuni.android.ui.dialog.WebViewDialogActivity
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.home.home.model.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class HomeFragment : Fragment(), HomeClickListener {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.app_exit_title))
                .setMessage(getString(R.string.app_exit_message))
                .setPositiveButton(getString(R.string.exit)) { d, _ -> activity?.finish() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .setOnCancelListener { }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onHomeResumed()
    }

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
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        appBar.disableDragging()

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = kotlin.math.abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            pageTitle?.alpha = 1f - ratio
        })

        with(homeList) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = HomeListAdapter(this@HomeFragment)
        }

        viewModel.showAddFamilyMemberDialog.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showAddFamilyMemberDialog()
            }
        })

        viewModel.showSuggestionDialog.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { triageProfile ->
                val intent =
                    Intent(ImmuniApplication.appContext, WebViewDialogActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("url", triageProfile.url)
                        putExtra("color", triageProfile.severity.backgroundColor())
                    }
                activity?.startActivity(intent)
            }
        })

        viewModel.homelistModel.observe(viewLifecycleOwner, Observer { newList ->
            (homeList.adapter as? HomeListAdapter)?.apply {
                update(newList)
            }
        })

        viewModel.blockingItemsListModel.observe(viewLifecycleOwner, Observer { newList ->
            // blocking cards
            val blockingItems = newList.filter {
                it is EnableGeolocationCard ||
                        it is EnableBluetoothCard ||
                        it is EnableNotificationCard ||
                        it is AddToWhiteListCard
            }
            if (blockingItems.isNotEmpty()) {
                showBlockingCard(blockingItems.first()!!)
            } else {
                hideBlockingCard()
            }
        })

        blockingCard.setOnClickListener { }
    }

    private fun showBlockingCard(item: HomeItemType) {

        blockingButton.setOnClickListener(null)
        blockingButton.setOnClickListener { onClick(item) }

        blockingIcon.setImageResource(
            when (item) {
                is EnableGeolocationCard -> {
                    if (item.type == GeolocationType.PERMISSIONS) R.drawable.ic_localization
                    else R.drawable.ic_localization
                }
                is EnableBluetoothCard -> R.drawable.ic_bluetooth
                is EnableNotificationCard -> R.drawable.ic_bell
                is AddToWhiteListCard -> R.drawable.ic_settings
                else -> R.drawable.ic_localization
            }
        )

        blockingTitle.text = when (item) {
            is EnableGeolocationCard -> {
                if (item.type == GeolocationType.PERMISSIONS) getString(R.string.home_block_permissions_title)
                else getString(R.string.home_block_geo_title)
            }
            is EnableBluetoothCard -> getString(R.string.home_block_bt_title)
            is EnableNotificationCard -> getString(R.string.home_block_notifications_title)
            is AddToWhiteListCard -> getString(R.string.home_block_whitelist_title)
            else -> ""
        }

        blockingMessageText.text = when (item) {
            is EnableGeolocationCard -> {
                if (item.type == GeolocationType.PERMISSIONS) getString(R.string.home_block_permissions_message)
                else getString(R.string.home_block_geo_message)
            }
            is EnableBluetoothCard -> getString(R.string.home_block_bt_message)
            is EnableNotificationCard -> getString(R.string.home_block_notifications_message)
            is AddToWhiteListCard -> getString(R.string.home_block_whitelist_message)
            else -> ""
        }

        blockingButton.text = when (item) {
            is EnableGeolocationCard -> {
                if (item.type == GeolocationType.PERMISSIONS) getString(R.string.home_block_permissions_button)
                else getString(R.string.home_block_geo_button)
            }
            is EnableBluetoothCard -> getString(R.string.home_block_bluetooth_button)
            is EnableNotificationCard -> getString(R.string.home_block_notifications_button)
            is AddToWhiteListCard -> getString(R.string.home_block_whitelist_button)
            else -> ""
        }

        (activity as? AppCompatActivity)?.setDarkStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        blockingCard.visible()
    }

    private fun hideBlockingCard() {
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        blockingCard.gone()
    }

    private fun showAddFamilyMemberDialog() {
        val action = HomeFragmentDirections.actionFamilyDialog()
        findNavController().navigate(action)
    }

    override fun onClick(item: HomeItemType) {
        when (item) {
            is SuggestionsCard -> {
                viewModel.openSuggestions(item.triageProfile)
            }
            is EnableNotificationCard -> {
                openNotificationDialog()
            }
            is EnableGeolocationCard -> {
                when (item.type) {
                    GeolocationType.PERMISSIONS -> openPermissionsDialog()
                    GeolocationType.GLOBAL_GEOLOCATION -> openGeolocationDialog()
                }
            }
            is EnableBluetoothCard -> {
                openBluetoothDialog()
            }
            is AddToWhiteListCard -> {
                openWhiteListDialog()
            }
            is SurveyCard -> {
                if (item.tapQuestion) {
                    openDiaryDialog()
                } else {

                }
            }
            is SurveyCardDone -> {
            }
        }
    }

    private fun openDiaryDialog() {
        val action = HomeFragmentDirections.actionDiaryDialog()
        findNavController().navigate(action)
    }

    private fun openWhiteListDialog() {
        // fixme
        //val action = HomeFragmentDirections.actionWhitelistDialog()
        //findNavController().navigate(action)
        // ExposureNotificationManager.startChangeBatteryOptimization(requireContext())
    }

    private fun openNotificationDialog() {
        val action = HomeFragmentDirections.actionNotificationsDialog()
        findNavController().navigate(action)
    }

    private fun openGeolocationDialog() {

    }

    private fun openPermissionsDialog() {
        // fixme
//        val permissionsManager: ExposureNotificationManager by inject()
//        if(permissionsManager.shouldShowPermissions(activity as AppCompatActivity,
//                *permissionsManager.geolocationPermissions())) {
//            permissionsManager.requestPermissions(activity as AppCompatActivity)
//        } else {
//            val action = HomeFragmentDirections.actionPermissionsDialog()
//            findNavController().navigate(action)
//        }
    }

    private fun openBluetoothDialog() {
        //val action = HomeFragmentDirections.actionBluetoothDialog()
        //findNavController().navigate(action)
        val bluetoothManager: BluetoothManager by inject()
        bluetoothManager.openBluetoothSettings(this)
    }
}