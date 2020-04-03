package org.immuni.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import org.immuni.android.R
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.onboarding_bluetooth_fragment.*

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
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }
}
