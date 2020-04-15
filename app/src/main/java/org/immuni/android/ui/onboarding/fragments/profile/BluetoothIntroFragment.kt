package org.immuni.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import org.immuni.android.R
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.service.ImmuniForegroundService
import org.koin.android.ext.android.inject

class BluetoothIntroFragment :
    ProfileContentFragment(R.layout.onboarding_bluetooth_fragment) {

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

            if(canProceed()) {
                viewModel.onNextTap()
                viewModel.onOnboardingComplete()
            } else {
                val action = ProfileFragmentDirections.actionPermissionsDialog()
                findNavController().navigate(action)
            }
        }
    }

    private fun canProceed(): Boolean {

        val bluetoothManager: BluetoothManager by inject()

        return !(!PermissionsManager.hasAllPermissions(requireContext()) ||
                !PermissionsManager.isIgnoringBatteryOptimizations(requireContext()) ||
                !PermissionsManager.globalLocalisationEnabled(requireContext()) ||
                !bluetoothManager.isBluetoothEnabled())
    }


    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }
}
