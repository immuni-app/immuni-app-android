package org.ascolto.onlus.geocrowd19.android.ui.home.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.dialog.AddFamilyMemberDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.GeolocationDisabledDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.NotificationsDisabledDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewActivity
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import org.ascolto.onlus.geocrowd19.android.ui.log.LogActivity
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.log_choose_person_fragment.next
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requireActivity().onBackPressedDispatcher.addCallback(this) {
            // users must select a choice
        //}
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

        viewModel.showAddFamilyMemberDialog.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showAddFamilyMemberDialog()
            }
        })

        next.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, LogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity?.startActivity(intent)
        }

        webViewButton.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, WebViewActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("url", "https://www.google.com")
            }
            activity?.startActivity(intent)
        }

        geolocation.setOnClickListener {
            GeolocationDisabledDialog().show(childFragmentManager, "geolocation_diabled_dialog")
        }

        notifications.setOnClickListener {
            NotificationsDisabledDialog().show(childFragmentManager, "notification_diabled_dialog")
        }

        addMembers.setOnClickListener {
            showAddFamilyMemberDialog()
        }
    }

    private fun showAddFamilyMemberDialog() {
        AddFamilyMemberDialog().show(childFragmentManager, "add_family_member")
    }
}