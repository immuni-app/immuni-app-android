package org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment.profile

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import org.ascolto.onlus.geocrowd19.android.R
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.add_relative_nickname_fragment.*
import kotlinx.android.synthetic.main.add_relative_nickname_fragment.next
import kotlinx.android.synthetic.main.add_relative_nickname_fragment.radioGroup
import kotlinx.android.synthetic.main.form_radio_field.*
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.models.Nickname
import org.ascolto.onlus.geocrowd19.android.models.NicknameType
import org.ascolto.onlus.geocrowd19.android.models.survey.SimpleAnswer
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.RelativeInfo

class NicknameFragment : CompoundButton.OnCheckedChangeListener, RelativeContentFragment(R.layout.add_relative_nickname_fragment){
    override val nextButton: View
        get() = next

    val items = mutableListOf<RadioButton>()

    override fun onResume() {
        super.onResume()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextButton.setOnClickListener {
            viewModel.onNextTap(LiveWithYouFragment::class.java)
        }

        enumValues<NicknameType>().forEach { type ->
            if(type == NicknameType.OTHER) {}
            else {
                enumValues<Gender>().forEach { gender ->
                    items.apply {
                        add(RadioButton(context).apply {
                            id = index
                            tag = index
                            text = answer
                            val tf = ResourcesCompat.getFont(context, R.font.euclid_circular_bold)
                            typeface = tf
                            textSize = 18f
                            buttonDrawable = ContextCompat.getDrawable(context, R.drawable.radio_button)
                            val paddingLeft = ScreenUtils.convertDpToPixels(context, 20)
                            val paddingTop = ScreenUtils.convertDpToPixels(context, 16)
                            val paddingBottom = ScreenUtils.convertDpToPixels(context, 16)
                            setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                            setTextColor(Color.parseColor("#495D74"))
                            setOnCheckedChangeListener(this@NicknameFragment)
                        })
                    }
                }
            }
        }

        radioGroup.apply {
            items.forEach { addView(it) }
        }
    }


    var lastRadioSelected = -1
    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) lastRadioSelected = buttonView?.id ?: -1
        if(!disableTriggeringEvent) validate()
    }

    override fun onUserInfoUpdate(userInfo: RelativeInfo) {
        disableTriggeringEvent = true
        items.forEach {
            it.isChecked = false
        }

        radioGroup.clearCheck()

        model.surveyAnswers[questionId]?.let { answers ->
            items.find {
                it.id == (answers.first() as SimpleAnswer).index
            }?.apply {
                isChecked = true
                lastRadioSelected = this.id
            }
        }

        disableTriggeringEvent = false
        validate(false)
    }

    fun validate(): Boolean {
        val valid = lastRadioSelected != -1
        nextButton.isEnabled = valid
        if (valid) saveData()
        return valid
    }

    private fun saveData() {
        if (lastRadioSelected != -1) {

            val nickname = Nickname(type, name)

            viewModel.userInfo()?.let {
                viewModel.updateUserInfo(it.copy(nickname = nickname))
            }
        }
    }

}
