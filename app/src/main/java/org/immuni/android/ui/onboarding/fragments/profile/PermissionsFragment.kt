package org.immuni.android.ui.onboarding.fragments.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bendingspoons.base.extensions.gone
import org.immuni.android.R
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.invisible
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.*
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.next
import kotlinx.android.synthetic.main.onboarding_permissions_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.toast
import org.immuni.android.ui.dialog.PermissionsTutorialDialog
import org.koin.android.ext.android.inject

class PermissionsFragment :
    ProfileContentFragment(R.layout.onboarding_permissions_fragment) {

    val permissionsManager: PermissionsManager by inject()
    val bluetoothManager: BluetoothManager by inject()

    var bluetoothExecuted = false
    var permissionsExecuted = false
    var geolocationExecuted = false

    override val nextButton: View
        get() = next

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bluetooth.setOnClickListener {
            bluetoothExecuted = true
            if(!bluetoothManager.isBluetoothSupported()) {
                toast(requireContext().getString(R.string.ble_not_supported_by_this_device))
                viewModel.onNextTap()
                return@setOnClickListener
            }
            if(!bluetoothManager.isBluetoothEnabled()) {

                bluetoothManager.openBluetoothSettings(this)
            }
        }

        geoPermissions.setOnClickListener {
            permissionsExecuted = true
            if(permissionsManager.shouldShowPermissions(activity as AppCompatActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                PermissionsTutorialDialog {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(500)
                        permissionsManager.requestPermissions(activity as AppCompatActivity)
                    }
                }.show(childFragmentManager, "tutorial")
            } else openAppSettings()
        }

        geolocation.setOnClickListener {
            geolocationExecuted = true
            PermissionsManager.startChangeGlobalGeolocalisation(requireContext())
        }

        viewModel.permissionsChanged.observe(viewLifecycleOwner, Observer {
            updateUI()
        })
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
            updateUI()
        }
    }

    private fun btON(): Boolean {
        return bluetoothManager.isBluetoothSupported() && bluetoothManager.isBluetoothEnabled()
    }

    private fun permissionsON(): Boolean {
        return PermissionsManager.hasAllPermissions(requireContext())
    }

    private fun geolocationON(): Boolean {
        return PermissionsManager.globalLocalisationEnabled(requireContext())
    }

    private fun updateUI() {

        // BLUETOOTH
        if(btON()) {
            // SUCCESS
            description.gone()
            number.gone()
            circle.setImageResource(R.drawable.ic_permissions_success)
            bluetooth.gone()
            bluetooth.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
        } else if(bluetoothExecuted && !btON()) {
            // FAIL
            description.gone()
            number.gone()
            circle.setImageResource(R.drawable.ic_permissions_error)
            bluetooth.setBackgroundColor(requireContext().resources.getColor(R.color.danger))
            bluetooth.visible()
        } else {
            // SHOW OPEN ALWAYS
            description.visible()
            number.visible()
            bluetooth.visible()
            circle.setImageResource(R.drawable.ic_permissins_bg)
            bluetooth.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
        }

        // PERMISSIONS
        if(permissionsON()) {
            // SUCCESS
            description2.gone()
            number2.gone()
            circle2.setImageResource(R.drawable.ic_permissions_success)
            geoPermissions.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
            geoPermissions.gone()
            bluetoothBox2.isEnabled = true
        } else if(permissionsExecuted && !permissionsON()) {
            // FAIL
            description2.gone()
            number2.gone()
            circle2.setImageResource(R.drawable.ic_permissions_error)
            geoPermissions.visible()
            geoPermissions.setBackgroundColor(requireContext().resources.getColor(R.color.danger))
            bluetoothBox2.isEnabled = true
        } else {
            // DISABLED
            if(!bluetoothExecuted && !btON()) {
                description2.gone()
                number2.visible()
                geoPermissions.gone()
                circle2.setImageResource(R.drawable.ic_permissions_bg_disabled)
                bluetoothBox2.isEnabled = false
            } else {
                // ACTIVE
                description2.visible()
                number2.visible()
                geoPermissions.visible()
                circle2.setImageResource(R.drawable.ic_permissins_bg)
                geoPermissions.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
                bluetoothBox2.isEnabled = true
            }
        }

        if(PermissionsManager.globalLocalisationEnabled(requireContext())) {
            // SUCCESS
            description3.gone()
            number3.gone()
            circle3.setImageResource(R.drawable.ic_permissions_success)
            geolocation.gone()
            geolocation.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
            bluetoothBox3.isEnabled = true
        } else if(geolocationExecuted && !geolocationON()){
            // FAIL
            description3.gone()
            number3.gone()
            circle3.setImageResource(R.drawable.ic_permissions_error)
            geolocation.setBackgroundColor(requireContext().resources.getColor(R.color.danger))
            geolocation.visible()
            bluetoothBox3.isEnabled = true
        }else {
            // DISABLED
            if((!permissionsExecuted && !permissionsON()) || (!bluetoothExecuted && !btON())) {
                description3.gone()
                number3.visible()
                geolocation.gone()
                circle3.setImageResource(R.drawable.ic_permissions_bg_disabled)
                bluetoothBox3.isEnabled = false
            } else {
                // ACTIVE
                description3.visible()
                number3.visible()
                geolocation.visible()
                circle3.setImageResource(R.drawable.ic_permissins_bg)
                geolocation.setBackgroundColor(requireContext().resources.getColor(R.color.colorPrimary))
                bluetoothBox3.isEnabled = true
            }
        }

        if((bluetoothExecuted || btON())
            && (permissionsExecuted || permissionsON())
            && (geolocationExecuted || geolocationON())) {
            nextButton.visible()
            nextButton.isEnabled = true
        } else {
            nextButton.invisible()
            nextButton.isEnabled = false
        }
    }

    /*
    private fun openPermissionsTutorialDialog() {
        PermissionsTutorialDialog {
            permissionsManager.requestPermissions(activity as AppCompatActivity)
        }.show(childFragmentManager, "permissions_tutorial")
    }
     */

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }
}
