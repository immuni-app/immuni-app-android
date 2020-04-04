package org.immuni.android.ui.home.home.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enable_app_permissions_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment

class PermissionsDialogFragment: FullScreenDialogLightFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.enable_app_permissions_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            openAppSettings()

            //val geolocationManager: GeolocationManager by inject()
            //geolocationManager.requestPermissions(activity = activity as AppCompatActivity)

        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        question.setOnClickListener { navigateToExplanations() }
        questionText.setOnClickListener { navigateToExplanations() }
    }

    fun navigateToExplanations() {
        val action =
            PermissionsDialogFragmentDirections.actionPermissionExplanationDialog()
        findNavController().navigate(action)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
        startActivity(intent)

        findNavController().popBackStack()
    }
}