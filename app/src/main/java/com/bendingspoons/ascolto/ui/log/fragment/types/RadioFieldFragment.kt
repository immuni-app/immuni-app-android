package com.bendingspoons.ascolto.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.CompoundButton.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.models.survey.RadioWidget
import com.bendingspoons.ascolto.models.survey.Survey
import com.bendingspoons.ascolto.ui.log.fragment.FormContentFragment
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.form_radio_field.*
import kotlinx.android.synthetic.main.form_radio_field.view.*
import kotlinx.android.synthetic.main.form_text_field.next

class RadioFieldFragment: FormContentFragment(R.layout.form_radio_field), OnCheckedChangeListener {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val questionText: TextView
        get() = question
    override val descriptionText: TextView
        get() = description

    val items = mutableListOf<RadioButton>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.survey.observe(viewLifecycleOwner, Observer {
            buildWidget(it)
        })
    }

    private fun buildWidget(it: Survey) {
        val question = it.questions.first { it.id == questionId }

        questionText.text = question.title
        descriptionText.text = question.description

        if(question.description.isEmpty()) descriptionText.gone()
        else descriptionText.visible()

        val widget = question.widget as RadioWidget
        widget.answers.forEachIndexed { index, answer ->
            items.apply {
                add(RadioButton(context).apply {
                    tag = "$index"
                    text = answer
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