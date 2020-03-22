package org.ascolto.onlus.geocrowd19.android.ui.home.family

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_fragment.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.dialog.FamilyDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.GeolocationDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.NotificationsDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialogActivity
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
            val intent = Intent(AscoltoApplication.appContext, WebViewDialogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("url", "https://content.ascolto-onlus.org/77e2e08cd1b72d9529493b8fabcb8804/5b35a7d7f1fa8119cde5d6702806cbb4.html#/temperature/warning")
            }
            activity?.startActivity(intent)
        }

        geolocation.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, GeolocationDialogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity?.startActivity(intent)
        }

        notifications.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, NotificationsDialogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity?.startActivity(intent)
        }

        addMembers.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, FamilyDialogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity?.startActivity(intent)
        }
    }
}