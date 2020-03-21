package org.ascolto.onlus.geocrowd19.android.ui.log.fragment.types

import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.CompoundButton.*
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.FormContentFragment
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import kotlinx.android.synthetic.main.form_radio_field.*
import kotlinx.android.synthetic.main.form_radio_field.back
import kotlinx.android.synthetic.main.form_something_changed_field.*
import kotlinx.android.synthetic.main.form_something_changed_field.question
import kotlinx.android.synthetic.main.form_text_field.next

class SomethingChangedFieldFragment: FormContentFragment(R.layout.form_something_changed_field), OnCheckedChangeListener {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val questionText: TextView
        get() = question
    override val descriptionText: TextView
        get() = description

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yes.setOnCheckedChangeListener { _, _ -> validate() }
        no.setOnCheckedChangeListener { _, _ -> validate() }

        next.setOnClickListener(null)
        next.setOnClickListener {
            if(yes.isChecked) {
                viewModel.onNextTap(null)
            } else {
                toast("NO decide what to do next")
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        validate()
    }

    override fun onFormModelUpdate(model: FormModel) {

    }

    override fun validate(): Boolean {
        val valid = yes.isChecked || no.isChecked
        nextButton.isEnabled = valid
        return valid
    }
}