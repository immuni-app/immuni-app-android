package org.ascolto.onlus.geocrowd19.android.ui.home.family

import PushNotificationUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_fragment.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.dialog.AddFamilyMemberDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.GeolocationDisabledDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.NotificationsDisabledDialog
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialog
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import org.ascolto.onlus.geocrowd19.android.ui.log.LogActivity
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FamilyFragment : Fragment() {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requireActivity().onBackPressedDispatcher.addCallback(this) {
            // users must select a choice
        //}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.family_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        next.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, LogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity?.startActivity(intent)
        }

        webViewButton.setOnClickListener {
            WebViewDialog().apply {
                arguments = bundleOf("url" to "https://www.google.com")
            }.show(childFragmentManager, "webview_dialog")
        }

        geolocation.setOnClickListener {
            GeolocationDisabledDialog().show(childFragmentManager, "geolocation_diabled_dialog")
        }

        notifications.setOnClickListener {
            NotificationsDisabledDialog().show(childFragmentManager, "notification_diabled_dialog")
        }

        addMembers.setOnClickListener {
            AddFamilyMemberDialog().show(childFragmentManager, "add_family_member")

            toast("Push notification enabled" + PushNotificationUtils.getPushNotificationState(requireContext()).toString())
        }
    }
}