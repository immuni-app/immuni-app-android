package com.bendingspoons.ascolto.ui.log.fragment.types

import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.CompoundButton.*
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import kotlinx.android.synthetic.main.form_radio_field.*
import kotlinx.android.synthetic.main.form_text_field.*
import kotlinx.android.synthetic.main.form_text_field.next

class RadioFieldFragment: FormContentFragment(R.layout.form_radio_field), OnCheckedChangeListener {
    override val nextButton: Button
        get() = next
    override val question: TextView
        get() = question
    override val description: TextView
        get() = description

    val items = mutableListOf<RadioButton>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items.apply {
            add(RadioButton(context).apply {
                tag = "0"
                text = "Text Dynamic"
                setOnCheckedChangeListener(this@RadioFieldFragment)
            })
            add(RadioButton(context).apply {
                tag = "1"
                text = "Text Dynamic 2"
                setOnCheckedChangeListener(this@RadioFieldFragment)
            })
            add(RadioButton(context).apply {
                tag = "2"
                text = "Text Dynamic 3"
                setOnCheckedChangeListener(this@RadioFieldFragment)
            })
        }

        radioGroup.apply {
            items.forEach { addView(it) }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        validate()
    }

    override fun onFormModelUpdate(model: FormModel) {

    }

    override fun validate(): Boolean {
        val valid = items.any { it.isChecked }
        nextButton.isEnabled = valid
        return valid
    }
}