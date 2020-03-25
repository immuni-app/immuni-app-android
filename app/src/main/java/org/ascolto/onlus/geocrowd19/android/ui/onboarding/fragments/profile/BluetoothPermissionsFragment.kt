package org.ascolto.onlus.geocrowd19.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.*
import org.ascolto.onlus.geocrowd19.android.ui.dialog.PermissionsTutorialDialog
import org.koin.android.ext.android.inject

class BluetoothPermissionsFragment : ProfileContentFragment(R.layout.onboarding_bluetooth_fragment) {
    val geolocationManager: GeolocationManager by inject()

    var tutorialOpened = false

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
            if(!tutorialOpened) {
                openPermissionsTutorialDialog()
                geolocationManager.requestPermissions(activity as AppCompatActivity)
            }
            else viewModel.onNextTap()
            //else geolocationManager.requestPermissions(activity as AppCompatActivity)
        }
    }

    private fun openPermissionsTutorialDialog() {
        PermissionsTutorialDialog {
            geolocationManager.requestPermissions(activity as AppCompatActivity)
        }.show(childFragmentManager, "permissions_tutorial")
        tutorialOpened = true
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }

    private fun validate(): Boolean {
        nextButton.isEnabled = true
        return true
    }
}
