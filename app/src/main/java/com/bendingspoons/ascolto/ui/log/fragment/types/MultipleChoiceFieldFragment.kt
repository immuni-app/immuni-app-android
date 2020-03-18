package com.bendingspoons.ascolto.ui.log.fragment.types

import android.os.Bundle
import android.view.View
import android.widget.*
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import kotlinx.android.synthetic.main.form_multiple_choice_field.*

class MultipleChoiceFieldFragment: FormContentFragment(R.layout.form_multiple_choice_field), CompoundButton.OnCheckedChangeListener {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val question: TextView
        get() = question
    override val description: TextView
        get() = description

    val items = mutableListOf<CheckBox>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items.apply {

            for(i in 0..20) {
                add(CheckBox(context).apply {
                    tag = "$i"
                    text = "Text $i"
                    setOnCheckedChangeListener(this@MultipleChoiceFieldFragment)
                })
            }
        }

        multipleChoiceGroup.apply {
            items.forEach { addView(it) }
        }
    }

    override fun onFormModelUpdate(model: FormModel) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        validate()
    }

    override fun validate(): Boolean {
        val valid = items.any{ it.isChecked }
        nextButton.isEnabled = valid
        return valid
    }


}