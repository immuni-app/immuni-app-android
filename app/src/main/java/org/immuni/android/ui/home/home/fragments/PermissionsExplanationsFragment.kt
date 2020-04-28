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

class PermissionsExplanationsFragment: FullScreenDialogLightFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.enable_permissions_explanations_dialog, container, false)
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
}