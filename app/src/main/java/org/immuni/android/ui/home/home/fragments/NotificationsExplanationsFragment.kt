package org.immuni.android.ui.home.home.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enable_notifications_explanations_dialog.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment

class NotificationsExplanationsFragment: FullScreenDialogLightFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.enable_notifications_explanations_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            findNavController().popBackStack()
        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun navigateToExplanations() {
        val action =
            NotificationsDialogFragmentDirections.actionGlobalNotificationsExplanations()
        findNavController().navigate(action)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
        startActivity(intent)
    }

    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, ImmuniApplication.appContext.packageName)
            startActivity(intent)
        } else {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
            startActivity(intent)
        }
    }
}