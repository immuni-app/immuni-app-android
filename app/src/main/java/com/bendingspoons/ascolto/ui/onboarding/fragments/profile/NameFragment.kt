package com.bendingspoons.ascolto.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.showKeyboard
import kotlinx.android.synthetic.main.onboarding_name_fragment.*

class NameFragment : ProfileContentFragment(R.layout.onboarding_name_fragment) {
    override val nextButton: View
        get() = next

    override fun onResume() {
        super.onResume()
        this.view?.showKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }
/*
    private fun updateGender(gender: Gender?) {
        userInfo()?.let {
            updateUserInfo(
                it.copy(gender = gender)
            )
        }
    }



    private fun updateUI(gender: Gender?) {
        female.isActivated = gender == Gender.FEMALE
        male.isActivated = gender == Gender.MALE
        next.isEnabled = gender != null
    } */
}
