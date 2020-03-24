package org.ascolto.onlus.geocrowd19.android.ui.onboarding.fragments.profile

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.core.widget.doOnTextChanged
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingUserInfo
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.showKeyboard
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.onboarding_age_range_fragment.*

class AgeRangeFragment : ProfileContentFragment(R.layout.onboarding_age_range_fragment),
                            CompoundButton.OnCheckedChangeListener{
    override val nextButton: View
        get() = next

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        nextButton.setOnClickListener(null) // override the default behaviour
        nextButton.setOnClickListener {

            if(lastRadioSelected == R.id.age_range_0_17) {
                MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.onboarding_age_more_than_18_title))
                    .setMessage(getString(R.string.onboarding_age_more_than_18_message))
                    .setPositiveButton(getString(R.string.confirm)) { d, _ -> viewModel.onNextTap() }
                    .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                    .setOnCancelListener { }
                    .show()
            } else {
                viewModel.onNextTap()
            }
        }

        back.setOnClickListener {
            viewModel.onPrevTap()
        }

        age_range_0_17.setOnCheckedChangeListener(this)
        age_range_18_35.setOnCheckedChangeListener(this)
        age_range_36_45.setOnCheckedChangeListener(this)
        age_range_46_55.setOnCheckedChangeListener(this)
        age_range_56_65.setOnCheckedChangeListener(this)
        age_range_66_75.setOnCheckedChangeListener(this)
        age_range_75.setOnCheckedChangeListener(this)
    }

    override fun onUserInfoUpdate(userInfo: OnboardingUserInfo) {
        disableTriggeringEvent = true
        updateUI(userInfo.ageGroup)
        validate(false)
        disableTriggeringEvent = false
    }

    private fun validate(updateModel: Boolean = true): Boolean {
        var valid = lastRadioSelected != -1
        nextButton.isEnabled = valid
        if(valid && updateModel) updateModel(lastRadioSelected)
        return valid
    }

    private fun updateModel(id: Int) {
        viewModel.userInfo()?.let {
            viewModel.updateUserInfo(it.copy(ageGroup = when(id) {
                R.id.age_range_0_17 -> "0-17"
                R.id.age_range_18_35 -> "18-35"
                R.id.age_range_36_45 -> "36-45"
                R.id.age_range_46_55 -> "46-55"
                R.id.age_range_56_65 -> "56-65"
                R.id.age_range_66_75 -> "66-75"
                else -> "75+"
            }))
        }
    }

    private fun updateUI(string: String?) {
        radioGroup.clearCheck()

        age_range_0_17.isChecked = false
        age_range_18_35.isChecked = false
        age_range_36_45.isChecked = false
        age_range_46_55.isChecked = false
        age_range_56_65.isChecked = false
        age_range_66_75.isChecked = false
        age_range_75.isChecked = false

        when(string) {
            "0-17" -> age_range_0_17.isChecked = true
            "18-35" -> age_range_18_35.isChecked = true
            "36-45" -> age_range_36_45.isChecked = true
            "46-55" -> age_range_46_55.isChecked = true
            "56-65" -> age_range_56_65.isChecked = true
            "66-75" -> age_range_66_75.isChecked = true
            "75+" -> age_range_75.isChecked = true
        }

        when(string) {
            "0-17" -> lastRadioSelected = R.id.age_range_0_17
            "18-35" -> lastRadioSelected = R.id.age_range_18_35
            "36-45" -> lastRadioSelected = R.id.age_range_36_45
            "46-55" -> lastRadioSelected = R.id.age_range_46_55
            "56-65" -> lastRadioSelected = R.id.age_range_56_65
            "66-75" -> lastRadioSelected = R.id.age_range_66_75
            "75+" -> lastRadioSelected = R.id.age_range_75
        }
    }

    var lastRadioSelected = -1
    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) lastRadioSelected = buttonView?.id ?: -1
        if(!disableTriggeringEvent) validate(true)
    }
}
