package org.immuni.android.ui.addrelative.fragment.profile

import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.View.FOCUS_DOWN
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.bendingspoons.base.extensions.gone
import org.immuni.android.R
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.showKeyboard
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.add_relative_nickname_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.db.entity.Gender
import org.immuni.android.models.Nickname
import org.immuni.android.models.NicknameType
import org.immuni.android.ui.addrelative.RelativeInfo

class NicknameFragment : CompoundButton.OnCheckedChangeListener,
    RelativeContentFragment(R.layout.add_relative_nickname_fragment) {
    override val nextButton: View
        get() = next
    override val prevButton: View
        get() = back

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

        val mainUserAgeGroup = viewModel.mainUser.ageGroup

        enumValues<NicknameType>().forEach { type ->
            val ageGroup = userInfo.ageGroup!!
            val gender = userInfo.gender!!

            if (type == NicknameType.OTHER) {
            } // at the end
            else if (ageGroup < mainUserAgeGroup && type in setOf(
                    NicknameType.OLDER_SIBLING,
                    NicknameType.PARENT,
                    NicknameType.MATERNAL_GRANDPARENT,
                    NicknameType.PATERNAL_GRANDPARENT
                )) {
            } // skip
            else if (ageGroup > mainUserAgeGroup && type in setOf(
                    NicknameType.CHILD_1,
                    NicknameType.CHILD_2,
                    NicknameType.CHILD_3,
                    NicknameType.CHILD_4,
                    NicknameType.YOUNGER_SIBLING
                )) {
            } // skip
            else {
                items.apply {
                    put(
                        Pair(type, gender),
                        radioButton(cont++, Nickname(type, "").humanReadable(requireContext(), gender))
                    )
                }
            }
        }

        // place OTHERS at the end
        items.apply {
            put(
                Pair(NicknameType.OTHER, Gender.FEMALE),
                radioButton(cont++, requireContext().getString(R.string.choose_a_nickname))
                )
        }

        radioGroup.apply {
            items.values.forEach { addView(it) }
        }

        textField.filters = mutableListOf<InputFilter>().apply {
            addAll(textField.filters)
            add(InputFilter.LengthFilter(5))
        }.toTypedArray()
        textField.doOnTextChanged { text, _, _, _ ->
            items.keys.filter { key -> key.first == NicknameType.OTHER}.forEach {
                items[it]?.isChecked = true
            }
            validate(true)
        }

        textField.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                items.keys.filter { key -> key.first == NicknameType.OTHER}.forEach {
                    items[it]?.isChecked = true
                }
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    scrollView.fullScroll(FOCUS_DOWN)
                }
            }
        }
    }


    var lastRadioSelected: NicknameType? = null
    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            items.keys.forEach { key ->
                if (items[key] == buttonView) {
                    lastRadioSelected = key.first
                }
            }
        }
        if (!disableTriggeringEvent) {

            if (lastRadioSelected == NicknameType.OTHER) textField.showKeyboard()
            else {
                view?.hideKeyboard()
                textField.clearFocus()
            }

            validate()
        }
    }

    override fun onUserInfoUpdate(userInfo: RelativeInfo) {
        disableTriggeringEvent = true
        items.values.forEach {
            it.isChecked = false
        }

        radioGroup.clearCheck()

        val type = userInfo.nickname?.type
        //val gender = userInfo.gender!!

        items.keys.filter { key -> key.first == type}.forEach {
            items[it]?.isChecked = true
        }
        lastRadioSelected = type
        disableTriggeringEvent = false
        validate(false)
    }

    fun validate(updateModel: Boolean = true): Boolean {
        var valid = lastRadioSelected != null

        if (lastRadioSelected == NicknameType.OTHER) {
            //editTextGroup.visible()
            valid = valid && textField.text.toString().isNotEmpty() && textField.text.toString().length <= 5
        } else {
            //editTextGroup.gone()
        }

        if (valid && updateModel) saveData()
        nextButton.isEnabled = valid
        return valid
    }

    private fun saveData() {
        lastRadioSelected?.let { type ->

            val customName = when (type) {
                NicknameType.OTHER -> textField.text.toString()
                else -> null
            }

            val nickname = Nickname(type, customName)

            viewModel.userInfo()?.let {
                viewModel.updateUserInfo(it.copy(nickname = nickname))
            }
        }
    }

    private fun radioButton(id: Int, text: String): RadioButton {
        return RadioButton(requireContext()).apply {
            this.id = id
            tag = id
            this.text = text
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
        }
    }
}
