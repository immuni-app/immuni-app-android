package org.immuni.android.ui.onboarding.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.immuni.android.R
import org.immuni.android.managers.GeolocationManager
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.*
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.ui.dialog.PermissionsTutorialDialog
import org.koin.android.ext.android.inject

class BluetoothPermissionsFragment : ProfileContentFragment(R.layout.onboarding_bluetooth_fragment) {
    val geolocationManager: GeolocationManager by inject()
    val bluetoothManager: BluetoothManager by inject()

    var alreadyAskedBluetooth = false

    override val nextButton: View
        get() = next

    override fun onResume() {
        super.onResume()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back.setOnClickListener {
            viewModel.onPrevTap()
        }

        next.isEnabled = true
        next.setOnClickListener(null)
        next.setOnClickListener {
            navigateNext()
        }
    }

    private fun navigateNext() {
        if(!bluetoothManager.isBluetoothEnabled() && !alreadyAskedBluetooth) {
            bluetoothManager.openBluetoothSettings(this)
            alreadyAskedBluetooth = true
        } else  {
            geolocationManager.requestPermissions(activity as AppCompatActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
            navigateNext()
        }
    }

    private fun openPermissionsTutorialDialog() {
        PermissionsTutorialDialog {
            geolocationManager.requestPermissions(activity as AppCompatActivity)
        }.show(childFragmentManager, "permissions_tutorial")
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }
}
