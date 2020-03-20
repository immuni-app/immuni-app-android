package com.bendingspoons.ascolto.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.toast
import com.bendingspoons.ascolto.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.onboarding_geolocation_fragment.*

class GeolocationPermissionsFragment : ProfileContentFragment(R.layout.onboarding_geolocation_fragment) {
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

        geolocation.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle("Aggancitati qui SR")
                .setMessage("Buon lavoro =)")
                .setPositiveButton(getString(R.string.confirm)) { d, _ -> validate() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .setOnCancelListener {  }
                .show()
        }
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }

    private fun validate(): Boolean {
        nextButton.isEnabled = true
        return true
    }
}
