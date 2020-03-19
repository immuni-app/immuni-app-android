package com.bendingspoons.ascolto.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.CompoundButton.*
import androidx.core.content.res.ResourcesCompat
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.form_radio_field.*
import kotlinx.android.synthetic.main.form_text_field.next

class RadioFieldFragment: FormContentFragment(R.layout.form_radio_field), OnCheckedChangeListener {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val question: TextView
        get() = question
    override val description: TextView
        get() = description

    val items = mutableListOf<RadioButton>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        items.apply {
            for(i in 1..4) {
                add(RadioButton(context).apply {
                    tag = "0"
                    text = "Scelta singola medio lunga"
                    val tf = ResourcesCompat.getFont(context, R.font.euclid_circular_bold)
                    typeface = tf
                    textSize = 18f
                    val paddingLeft = ScreenUtils.convertDpToPixels(context, 4)
                    val paddingTop = ScreenUtils.convertDpToPixels(context, 8)
                    val paddingBottom = ScreenUtils.convertDpToPixels(context, 8)
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                    setTextColor(Color.parseColor("#495D74"))
                    setOnCheckedChangeListener(this@RadioFieldFragment)
                })
            }
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