package com.bendingspoons.ascolto.ui.log.fragment.types

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.form_picker_field.*
import kotlinx.android.synthetic.main.form_text_field.next


class PickerFieldFragment: FormContentFragment(R.layout.form_picker_field) {
    override val nextButton: Button
        get() = next
    override val question: TextView
        get() = question
    override val description: TextView
        get() = description

    val items = mutableListOf<NumberPicker>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        items.apply {
            add(NumberPicker(context).apply {
                // IMPORTANT! setMinValue to 1 and call setDisplayedValues after setMinValue and setMaxValue
                val data =
                    arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I")
                minValue = 1
                maxValue = data.size
                displayedValues = data
                value = 1
                setOnValueChangedListener { picker, oldVal, newVal ->
                    validate()
                }
            })
        }

        pickerGroup.apply {
            items.forEach { addView(it) }
        }

        validate()
    }

    override fun onFormModelUpdate(model: FormModel) {

    }

    override fun validate(): Boolean {
        val valid = true
        nextButton.isEnabled = valid
        return valid
    }
}