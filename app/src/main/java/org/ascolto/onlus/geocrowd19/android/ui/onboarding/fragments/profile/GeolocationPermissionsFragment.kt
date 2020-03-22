package org.ascolto.onlus.geocrowd19.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingUserInfo
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingViewModel
import com.bendingspoons.base.extensions.hideKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.onboarding_geolocation_fragment.*
import org.koin.android.ext.android.inject

class GeolocationPermissionsFragment : ProfileContentFragment(R.layout.onboarding_geolocation_fragment) {
    val geolocationManager: GeolocationManager by inject()

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
            geolocationManager.requestPermissions(activity as AppCompatActivity)
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
