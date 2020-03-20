package com.bendingspoons.ascolto.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.db.entity.Gender
import com.bendingspoons.ascolto.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.onboarding_gender_fragment.*

class GenderFragment : ProfileContentFragment(R.layout.onboarding_gender_fragment) {
    override val nextButton: View
        get() = next

    override fun onResume() {
        super.onResume()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        male.setOnClickListener {
            if(validate()) {
                viewModel.userInfo()?.let {
                    viewModel.updateUserInfo(it.copy(gender = Gender.MALE))
                }
            }
        }

        female.setOnClickListener {
            if(validate()) {
                viewModel.userInfo()?.let {
                    viewModel.updateUserInfo(it.copy(gender = Gender.FEMALE))
                }
            }
        }

        back.setOnClickListener {
            viewModel.onPrevTap()
        }
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        //updateUI(userInfo.gender)
    }

    private fun validate(): Boolean {
        val valid = male.isChecked || female.isChecked
        nextButton.isEnabled = valid
        return valid
    }
}
