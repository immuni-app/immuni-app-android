package org.immuni.android.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.enable_geolocation_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import org.immuni.android.AscoltoActivity
import org.immuni.android.AscoltoApplication
import org.immuni.android.R
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.BluetoothManager.Companion.REQUEST_ENABLE_BT
import org.immuni.android.toast
import org.koin.android.ext.android.inject

class BluetoothDialogActivity: AscoltoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enable_bluetooth_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.transparent))

        button.setOnClickListener {
            val btManager: BluetoothManager by inject()
            if(!btManager.isBluetoothSupported()) {
                toast(applicationContext.getString(R.string.ble_not_supported_by_this_device))
                return@setOnClickListener
            }

            btManager.openBluetoothSettings(this, REQUEST_ENABLE_BT)
        }

        back.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT) {
            finish()
        }
    }
}