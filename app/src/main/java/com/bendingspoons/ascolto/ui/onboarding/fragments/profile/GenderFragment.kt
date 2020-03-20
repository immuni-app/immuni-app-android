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
            validate(true)
        }

        female.setOnClickListener {
            validate(true)
        }

        back.setOnClickListener {
            viewModel.onPrevTap()
        }
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        updateUI(userInfo.gender)
        validate(false)
    }

    private fun validate(updateModel: Boolean = true): Boolean {
        val valid = male.isChecked || female.isChecked
        nextButton.isEnabled = valid
        if(valid && updateModel) updateModel(when {
            male.isChecked -> Gender.MALE
            else -> Gender.FEMALE
        })
        return valid
    }

    private fun updateModel(gender: Gender) {
        viewModel.userInfo()?.let {
            viewModel.updateUserInfo(it.copy(gender = gender))
        }
    }

    private fun updateUI(gender: Gender?) {
        when(gender) {
            Gender.MALE -> {
                male.isChecked = true
                female.isChecked = false
            }
            Gender.FEMALE  -> {
                male.isChecked = false
                female.isChecked = true
            }
            else -> {
                male.isChecked = false
                female.isChecked = false
            }
        }
    }
}
