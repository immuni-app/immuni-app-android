package org.ascolto.onlus.geocrowd19.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.showKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.onboarding_age_fragment.*

class AgeRangeFragment : ProfileContentFragment(R.layout.onboarding_age_range_fragment) {
    override val nextButton: View
        get() = next

    override fun onResume() {
        super.onResume()
        textField.showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        textField.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textField.doOnTextChanged { text, _, _, _ ->
            validate(true)
        }

        nextButton.setOnClickListener(null) // override the default behaviour
        nextButton.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(String.format(getString(R.string.onboarding_age_confirmation_title, textField.text.toString().toIntOrNull() ?: 0)))
                .setMessage(getString(R.string.onboarding_age_confirmation_message))
                .setPositiveButton(getString(R.string.confirm)) { d, _ -> viewModel.onNextTap() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .setOnCancelListener {  }
                .show()
        }

        back.setOnClickListener {
            viewModel.onPrevTap()
        }
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        updateUI(userInfo.age)
        validate(false)
    }

    private fun validate(updateModel: Boolean = true): Boolean {
        val text = textField.text.toString().trim()
        var valid = text.isNotEmpty()
        valid = valid && (text.toIntOrNull() != null)
        nextButton.isEnabled = valid
        if(valid && updateModel) updateModel(text.toInt())
        return valid
    }

    private fun updateModel(age: Int) {
        viewModel.userInfo()?.let {
            viewModel.updateUserInfo(it.copy(age = age))
        }
    }

    private fun updateUI(age: Int?) {
        updateEditText(textField, age?.toString() ?: "")
    }
}
