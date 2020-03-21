package org.ascolto.onlus.geocrowd19.android.ui.home.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.dialog.AddFamilyMemberDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.GeolocationDisabledDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.NotificationsDisabledDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialog
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import org.ascolto.onlus.geocrowd19.android.ui.log.LogActivity
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.log_choose_person_fragment.next
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.HomeItemType
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel
import java.lang.Math.abs

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

        viewModel.listModel.observe(viewLifecycleOwner, Observer {
            (homeList.adapter as? HomeListAdapter)?.items?.apply {
                clear()
                //addAll(it)
            }

            homeList.adapter?.notifyDataSetChanged()
        })
    }

    private fun showAddFamilyMemberDialog() {
        AddFamilyMemberDialog().show(childFragmentManager, "add_family_member")
    }

    override fun onClick(item: HomeItemType) {
        toast(item.toString())
    }
}