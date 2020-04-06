package org.immuni.android.ui.home.family.details.edit

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.hideKeyboard
import com.bendingspoons.base.extensions.showKeyboard
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.user_edit_nickname_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.R
import org.immuni.android.db.entity.Gender
import org.immuni.android.loading
import org.immuni.android.models.Nickname
import org.immuni.android.models.NicknameType
import org.immuni.android.models.User
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class EditNicknameFragment : BaseEditFragment(), CompoundButton.OnCheckedChangeListener {

    val args by navArgs<EditNicknameFragmentArgs>()

    private lateinit var viewModel: EditDetailsViewModel
    val items = LinkedHashMap<Pair<NicknameType, Gender>, RadioButton>()

    override fun onPause() {
        super.onPause()
        textField?.hideKeyboard()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_edit_nickname_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel { parametersOf(args.userId)}

        viewModel.navigateBack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().popBackStack()
            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            buildWidget(it)
            items.values.forEach { it.isChecked = false}

            items.keys.filter { key -> key.first == it.nickname?.type}.forEach {
                items[it]?.isChecked = true
            }

            if(it.nickname?.type == NicknameType.OTHER) {
                textField.setText(it.nickname.value ?: "")
                textField.setSelection(textField.text.length)
                //editTextGroup.visible()
            } else {
                //editTextGroup.gone()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it)
        })

        back.setOnClickListener { findNavController().popBackStack() }
        textField.filters = mutableListOf<InputFilter>().apply {
            addAll(textField.filters)
            add(InputFilter.LengthFilter(5))
        }.toTypedArray()
        textField.doOnTextChanged { text, _, _, _ ->
            items.keys.filter { key -> key.first == NicknameType.OTHER}.forEach {
                items[it]?.isChecked = true
            }
            validate()
        }
        update.setOnClickListener {
            var nickname: Nickname? = null

            items.keys.forEach { pair ->
                val radio = items[pair]
                if(radio?.isChecked == true) {
                    nickname = if(pair.first == NicknameType.OTHER) {
                        Nickname(NicknameType.OTHER, textField.text.toString().trim())
                    } else {
                        Nickname(pair.first)
                    }
                }
            }

            val user = viewModel.user()
            user?.let {
                viewModel.updateUser(user.copy(nickname = nickname))
            }
        }

        textField.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }

            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        var selectedType: NicknameType? = null
        if (isChecked) {
            items.keys.forEach { key ->
                if (items[key] == buttonView) {
                    selectedType = key.first
                }
            }
            if (selectedType == NicknameType.OTHER) {
                //editTextGroup.visible()
                textField.showKeyboard()
            }
            else {
                //editTextGroup.gone()
                textField.hideKeyboard()
            }
        }

        validate()
    }

    private fun validate() {
        var selectedType: NicknameType? = null
        items.keys.forEach { pair ->
            val radio = items[pair]
            if(radio?.isChecked == true) {
                selectedType = pair.first
            }
        }

        var valid = selectedType != null

        if (selectedType == NicknameType.OTHER) {
            valid = valid && textField.text.toString().isNotEmpty() && textField.text.toString().length <= 5
        }

        update.isEnabled = valid
    }

    private fun buildWidget(user: User) {
        var cont = 0
        val mainUser = viewModel.mainUser()
        val currentType = user.nickname?.type!!
        val gender = user.gender

        enumValues<NicknameType>().forEach { type ->
            val ageGroup = user.ageGroup
            val gender = user.gender


            if (type == NicknameType.OTHER) {
            } // at the end
            else if (ageGroup < mainUser.ageGroup && type in setOf(
                    NicknameType.OLDER_SIBLING,
                    NicknameType.PARENT,
                    NicknameType.MATERNAL_GRANDPARENT,
                    NicknameType.PATERNAL_GRANDPARENT
                )) {
            } // skip
            else if (ageGroup > mainUser.ageGroup && type in setOf(
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

        // if the current type is not in the list (maybe because the ageGroup has changed)
        // we add it to prevent to have an empty field
        if(items.keys.none { it.first == currentType } && currentType != NicknameType.OTHER) {
            items.apply {
                put(
                    Pair(currentType, gender),
                    radioButton(cont++, Nickname(currentType, "").humanReadable(requireContext(), gender))
                )
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
            setOnCheckedChangeListener(this@EditNicknameFragment)
        }
    }
}
