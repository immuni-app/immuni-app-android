package com.bendingspoons.ascolto.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
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
        textField.showKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textField.doOnTextChanged { text, _, _, _ ->
            validate(true)
        }
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        updateUI(userInfo.name)
        validate(false)
    }

    private fun validate(updateModel: Boolean = true): Boolean {
        val valid = textField.text.toString().trim().isNotEmpty()
        nextButton.isEnabled = valid
        if(valid && updateModel) updateModel(textField.text.toString().trim())
        return valid
    }

    private fun updateModel(name: String) {
        viewModel.userInfo()?.let {
            viewModel.updateUserInfo(it.copy(name = name))
        }
    }

    private fun updateUI(name: String?) {
        textField.setText(name?.toString() ?: "")
    }

}
