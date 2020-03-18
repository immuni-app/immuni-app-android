package com.bendingspoons.ascolto.ui.log.fragment.types

import android.widget.Button
import android.widget.TextView
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import kotlinx.android.synthetic.main.form_text_field.*

class TextFieldFragment: FormContentFragment(R.layout.form_text_field) {
    override val nextButton: Button
        get() = next
    override val question: TextView
        get() = question
    override val description: TextView
        get() = description



    override fun onFormModelUpdate(model: FormModel) {

    }
}