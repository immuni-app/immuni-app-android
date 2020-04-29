package org.immuni.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import org.immuni.android.R
import org.immuni.android.models.Gender
import org.immuni.android.ui.onboarding.OnboardingUserInfo
import org.immuni.android.base.extensions.hideKeyboard
import kotlinx.android.synthetic.main.onboarding_gender_fragment.*
import kotlinx.android.synthetic.main.onboarding_gender_fragment.back
import kotlinx.android.synthetic.main.onboarding_gender_fragment.next

class GenderFragment : ProfileContentFragment(R.layout.onboarding_gender_fragment) {
    override val nextButton: View
        get() = next

    override fun onResume() {
        super.onResume()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        female.setOnClickListener {
            validate(true)
        }

        male.setOnClickListener {
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
        if (valid && updateModel) updateModel(if (male.isChecked) Gender.MALE else Gender.FEMALE)
        return valid
    }

    private fun updateModel(gender: Gender) {
        viewModel.userInfo()?.let {
            viewModel.updateUserInfo(it.copy(gender = gender))
        }
    }

    private fun updateUI(gender: Gender?) {
        male.isChecked = gender == Gender.MALE
        female.isChecked = gender == Gender.FEMALE
    }
}
