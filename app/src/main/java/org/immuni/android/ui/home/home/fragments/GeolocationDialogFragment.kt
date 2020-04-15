package org.immuni.android.ui.home.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.enable_notifications_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import org.immuni.android.R
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment

class GeolocationDialogFragment: FullScreenDialogDarkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.enable_geolocation_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            openGeolocationSettings()
        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun openGeolocationSettings() {
        PermissionsManager.startChangeGlobalGeolocalisation(requireContext())
        findNavController().popBackStack()
    }
}