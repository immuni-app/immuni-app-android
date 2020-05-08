package org.immuni.android.ui.home.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.home_fragment.*
import org.immuni.android.R
import org.immuni.android.extensions.activity.disableDragging
import org.immuni.android.extensions.activity.setLightStatusBarFullscreen
import org.immuni.android.managers.BluetoothManager
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

        viewModel.homelistModel.observe(viewLifecycleOwner, Observer { newList ->
            (homeList.adapter as? HomeListAdapter)?.apply {
                update(newList)
            }
        })
    }

    private fun showAddFamilyMemberDialog() {
        val action = HomeFragmentDirections.actionFamilyDialog()
        findNavController().navigate(action)
    }

    override fun onClick(item: HomeItemType) {
        when (item) {
            is SuggestionsCard -> {
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
        // val action = HomeFragmentDirections.actionWhitelistDialog()
        // findNavController().navigate(action)
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
        // val action = HomeFragmentDirections.actionBluetoothDialog()
        // findNavController().navigate(action)
        val bluetoothManager: BluetoothManager by inject()
        bluetoothManager.openBluetoothSettings(this)
    }
}
