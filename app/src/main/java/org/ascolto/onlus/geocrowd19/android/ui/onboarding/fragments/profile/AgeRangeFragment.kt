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
import org.ascolto.onlus.geocrowd19.android.models.AgeGroup

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
                R.id.age_range_0_17 -> AgeGroup.ZERO_SEVENTEEN
                R.id.age_range_18_35 -> AgeGroup.EIGHTEEN_THIRTYFIVE
                R.id.age_range_36_45 -> AgeGroup.THRITYSIX_FORTYFIVE
                R.id.age_range_46_55 -> AgeGroup.FORTYSIX_FIFTYFIVE
                R.id.age_range_56_65 -> AgeGroup.FIFTYSIX_SIXTYFIVE
                R.id.age_range_66_75 -> AgeGroup.SIXTYSIX_SEVENTYFIVE
                else -> AgeGroup.MORE_THAN_SEVENTYFIVE
            }))
        }
    }

    private fun updateUI(ageGroup: AgeGroup?) {
        radioGroup.clearCheck()

        age_range_0_17.isChecked = false
        age_range_18_35.isChecked = false
        age_range_36_45.isChecked = false
        age_range_46_55.isChecked = false
        age_range_56_65.isChecked = false
        age_range_66_75.isChecked = false
        age_range_75.isChecked = false

        when(ageGroup) {
            AgeGroup.ZERO_SEVENTEEN -> age_range_0_17.isChecked = true
            AgeGroup.EIGHTEEN_THIRTYFIVE -> age_range_18_35.isChecked = true
            AgeGroup.THRITYSIX_FORTYFIVE -> age_range_36_45.isChecked = true
            AgeGroup.FORTYSIX_FIFTYFIVE -> age_range_46_55.isChecked = true
            AgeGroup.FIFTYSIX_SIXTYFIVE -> age_range_56_65.isChecked = true
            AgeGroup.SIXTYSIX_SEVENTYFIVE -> age_range_66_75.isChecked = true
            AgeGroup.MORE_THAN_SEVENTYFIVE -> age_range_75.isChecked = true
        }

        when(ageGroup) {
            AgeGroup.ZERO_SEVENTEEN -> lastRadioSelected = R.id.age_range_0_17
            AgeGroup.EIGHTEEN_THIRTYFIVE -> lastRadioSelected = R.id.age_range_18_35
            AgeGroup.THRITYSIX_FORTYFIVE -> lastRadioSelected = R.id.age_range_36_45
            AgeGroup.FORTYSIX_FIFTYFIVE -> lastRadioSelected = R.id.age_range_46_55
            AgeGroup.FIFTYSIX_SIXTYFIVE -> lastRadioSelected = R.id.age_range_56_65
            AgeGroup.SIXTYSIX_SEVENTYFIVE -> lastRadioSelected = R.id.age_range_66_75
            AgeGroup.MORE_THAN_SEVENTYFIVE -> lastRadioSelected = R.id.age_range_75
        }
    }

    var lastRadioSelected = -1
    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) lastRadioSelected = buttonView?.id ?: -1
        if(!disableTriggeringEvent) validate(true)
    }
}
