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
import org.immuni.android.R
import org.immuni.android.ui.home.HomeSharedViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.home_fragment.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.models.survey.backgroundColor
import org.immuni.android.ui.dialog.*
import org.immuni.android.ui.home.home.model.*
import org.immuni.android.ui.log.LogActivity
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class HomeFragment : Fragment(), HomeClickListener {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.app_exit_title))
                .setMessage(getString(R.string.app_exit_message))
                .setPositiveButton(getString(R.string.exit)) { d, _ -> activity?.finish()}
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss()}
                .setOnCancelListener {  }
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
            it.getContentIfNotHandled()?.let { (url, severity) ->
                val intent = Intent(ImmuniApplication.appContext, WebViewDialogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("url", url)
                    putExtra("color", severity.backgroundColor())
                }
                activity?.startActivity(intent)
            }
        })

        viewModel.navigateToSurvey.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                val intent = Intent(ImmuniApplication.appContext, LogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                activity?.startActivity(intent)
            }
        })

        viewModel.homelistModel.observe(viewLifecycleOwner, Observer { newList ->
            (homeList.adapter as? HomeListAdapter)?.apply {
                update(newList)
            }

            //homeList.adapter?.notifyDataSetChanged()
        })
    }

    private fun showAddFamilyMemberDialog() {
        val action = HomeFragmentDirections.actionFamilyDialog()
        findNavController().navigate(action)
    }

    override fun onClick(item: HomeItemType) {
        when(item) {
            is SuggestionsCardWhite -> {
                viewModel.openSuggestions(item.severity)
            }
            is SuggestionsCardYellow -> {
                viewModel.openSuggestions(item.severity)
            }
            is SuggestionsCardRed -> {
                viewModel.openSuggestions(item.severity)
            }
            is EnableNotificationCard -> {
                openNotificationDialog()
            }
            is EnableGeolocationCard -> {
                when(item.type) {
                    GeolocationType.PERMISSIONS -> openPermissionsDialog()
                    GeolocationType.GLOBAL_GEOLOCATION -> openGeolocationDialog()
                }
            }
            is EnableBluetoothCard -> {
                openBluetoothDialog()
            }
            is SurveyCard -> {
                if(item.tapQuestion) {
                    openDiaryDialog()
                } else {
                    viewModel.onSurveyCardTap()
                }
            }
            is SurveyCardDone -> { }
        }
    }

    private fun openDiaryDialog() {
        val action = HomeFragmentDirections.actionDiaryDialog()
        findNavController().navigate(action)
    }

    private fun openNotificationDialog() {
        val action = HomeFragmentDirections.actionNotificationsDialog()
        findNavController().navigate(action)
    }

    private fun openGeolocationDialog() {
        val action = HomeFragmentDirections.actionGeolocationDialog()
        findNavController().navigate(action)
    }

    private fun openPermissionsDialog() {
        val action = HomeFragmentDirections.actionPermissionsDialog()
        findNavController().navigate(action)
    }

    private fun openBluetoothDialog() {
        val action = HomeFragmentDirections.actionBluetoothDialog()
        findNavController().navigate(action)
    }
}