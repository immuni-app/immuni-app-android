package org.ascolto.onlus.geocrowd19.android.ui.home.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.home_fragment.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.dialog.FamilyDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.GeolocationDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.NotificationsDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.*
import org.ascolto.onlus.geocrowd19.android.ui.log.LogActivity
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
            title?.alpha = 1f - ratio
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
            it.getContentIfNotHandled()?.let { url ->
                val intent = Intent(AscoltoApplication.appContext, WebViewDialogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("url", url)
                }
                activity?.startActivity(intent)
            }
        })

        viewModel.navigateToSurvey.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { url ->
                val intent = Intent(AscoltoApplication.appContext, LogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                activity?.startActivity(intent)
            }
        })

        viewModel.listModel.observe(viewLifecycleOwner, Observer {
            (homeList.adapter as? HomeListAdapter)?.items?.apply {
                clear()
                addAll(it)
            }

            homeList.adapter?.notifyDataSetChanged()
        })
    }

    private fun showAddFamilyMemberDialog() {
        val intent = Intent(AscoltoApplication.appContext, FamilyDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
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
                openGeolocationDialog()
            }
            is SurveyCard -> {
                viewModel.onSurveyCardTap()
            }
            is SurveyCardDone -> { }
        }
    }

    private fun openNotificationDialog() {
        val intent = Intent(AscoltoApplication.appContext, NotificationsDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
    }

    private fun openGeolocationDialog() {
        val intent = Intent(AscoltoApplication.appContext, GeolocationDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
    }
}