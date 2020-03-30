package org.immuni.android.ui.log.fragment.types

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import org.immuni.android.R
import org.immuni.android.ui.log.fragment.FormContentFragment
import org.immuni.android.ui.log.model.FormModel
import com.bendingspoons.base.extensions.showKeyboard
import kotlinx.android.synthetic.main.form_text_field.*

class TextFieldFragment: FormContentFragment(R.layout.form_text_field) {

    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val questionText: TextView
        get() = question
    override val descriptionText: TextView
        get() = description

    override fun onResume() {
        super.onResume()
        textField.showKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textField.doOnTextChanged { text, _, _, _ ->
            if(validate()) {
                // TODO save form data
            }
        }
    }

    override fun onFormModelUpdate(model: FormModel) {

    }

    override fun validate(save: Boolean): Boolean {
        nextButton.isEnabled = !textField.text.toString().isEmpty()
        return nextButton.isEnabled
    }
}