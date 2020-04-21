package org.immuni.android.ui.home.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.enable_app_permissions_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import org.immuni.android.R
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BluetoothManager.Companion.REQUEST_ENABLE_BT
import com.bendingspoons.base.extensions.toast
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment
import org.koin.android.ext.android.inject

class BluetoothDialogFragment: FullScreenDialogDarkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.enable_bluetooth_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            val btManager: BluetoothManager by inject()
            if(!btManager.isBluetoothSupported()) {
                toast(
                    requireContext(),
                    requireContext().getString(
                        R.string.ble_not_supported_by_this_device
                    )
                )
                return@setOnClickListener
            }

            btManager.openBluetoothSettings(this, REQUEST_ENABLE_BT)
        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT) {
            findNavController().popBackStack()
        }
    }
}