package org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment.profile

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import org.ascolto.onlus.geocrowd19.android.R
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.add_relative_nickname_fragment.next
import kotlinx.android.synthetic.main.add_relative_nickname_fragment.radioGroup
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.models.Nickname
import org.ascolto.onlus.geocrowd19.android.models.NicknameType
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.RelativeInfo

class NicknameFragment : CompoundButton.OnCheckedChangeListener, RelativeContentFragment(R.layout.add_relative_nickname_fragment){
    override val nextButton: View
        get() = next

    val items = LinkedHashMap<Pair<NicknameType, Gender>, RadioButton>()

    override fun onResume() {
        super.onResume()
        this.view?.hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextButton.setOnClickListener {
            viewModel.onNextTap(LiveWithYouFragment::class.java)
        }

        var cont = 0
        val userInfo = viewModel.userInfo()!!
        enumValues<NicknameType>().forEach { type ->
            if(type == NicknameType.OTHER) { } // at the end
            else if(userInfo.adult != true && type in setOf(
                        NicknameType.PARENT,
                        NicknameType.MATERNAL_GRANDPARENT,
                        NicknameType.PATERNAL_GRANDPARENT)) { } // skip
            else {
                items.apply {
                    val gender = userInfo.gender!!
                    put(
                        Pair(type, gender),
                        RadioButton(context).apply {
                        id = cont++
                        tag = cont
                        text = Nickname(type, "").humanReadable(context, gender)
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

        // place OTHERS at the end
        items.apply {
            put(
                Pair(NicknameType.OTHER, Gender.FEMALE),
                RadioButton(context).apply {
                id = cont++
                tag = cont
                text = context.getString(R.string.other)
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

        radioGroup.apply {
            items.values.forEach { addView(it) }
        }
    }


    var lastRadioSelected: NicknameType? = null
    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) {
            items.keys.forEach { key ->
                if(items[key] == buttonView) {
                    lastRadioSelected = key.first
                }
            }
        }
        if(!disableTriggeringEvent) validate()
    }

    override fun onUserInfoUpdate(userInfo: RelativeInfo) {
        disableTriggeringEvent = true
        items.values.forEach() {
            it.isChecked = false
        }

        radioGroup.clearCheck()

        val type = userInfo.nickname?.type
        val gender = userInfo.gender!!

        items[Pair(type, gender)]?.isChecked = true
        lastRadioSelected = type
        disableTriggeringEvent = false
        validate(false)
    }

    fun validate(updateModel: Boolean = true): Boolean {
        val valid = lastRadioSelected != null
        nextButton.isEnabled = valid
        if (valid && updateModel) saveData()
        return valid
    }

    private fun saveData() {
        lastRadioSelected?.let { type ->

            val nickname = Nickname(type, "TODO")

            viewModel.userInfo()?.let {
                viewModel.updateUserInfo(it.copy(nickname = nickname))
            }
        }
    }

}
