package com.bendingspoons.ascolto.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.utils.ScreenUtils
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
                    text = "Text $i descrizione medio lunga"
                    val tf = ResourcesCompat.getFont(context, R.font.euclid_circular_bold)
                    typeface = tf
                    textSize = 18f
                    val paddingLeft = ScreenUtils.convertDpToPixels(context, 4)
                    val paddingTop = ScreenUtils.convertDpToPixels(context, 8)
                    val paddingBottom = ScreenUtils.convertDpToPixels(context, 8)
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                    setTextColor(Color.parseColor("#495D74"))
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