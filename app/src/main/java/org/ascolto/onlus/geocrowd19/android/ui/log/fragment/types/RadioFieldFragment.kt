package org.ascolto.onlus.geocrowd19.android.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.CompoundButton.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.models.survey.CompositeAnswer
import org.ascolto.onlus.geocrowd19.android.models.survey.RadioWidget
import org.ascolto.onlus.geocrowd19.android.models.survey.SimpleAnswer
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.FormContentFragment
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.form_radio_field.*
import kotlinx.android.synthetic.main.form_radio_field.view.*
import kotlinx.android.synthetic.main.form_text_field.next

class RadioFieldFragment : FormContentFragment(R.layout.form_radio_field), OnCheckedChangeListener {
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

        if (question.description.isEmpty()) descriptionText.gone()
        else descriptionText.visible()

        val widget = question.widget as RadioWidget
        widget.answers.forEachIndexed { index, answer ->
            items.apply {
                add(RadioButton(context).apply {
                    id = index
                    tag = index
                    text = answer
                    val tf = ResourcesCompat.getFont(context, R.font.euclid_circular_bold)
                    typeface = tf
                    textSize = 18f
                    buttonDrawable = ContextCompat.getDrawable(context, R.drawable.radio_button)
                    val paddingLeft = ScreenUtils.convertDpToPixels(context, 20)
                    val paddingTop = ScreenUtils.convertDpToPixels(context, 16)
                    val paddingBottom = ScreenUtils.convertDpToPixels(context, 16)
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                    setTextColor(Color.parseColor("#495D74"))
                    setOnCheckedChangeListener(this@RadioFieldFragment)
                })
            }
        }

        radioGroup.apply {
            items.forEach { addView(it) }
        }

        formModel()?.let {
            onFormModelUpdate(it)
        }
    }

    var lastRadioSelected = -1
    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) lastRadioSelected = buttonView?.id ?: -1
        if(!disableTriggeringEvent) validate()
    }

    override fun onFormModelUpdate(model: FormModel) {
        disableTriggeringEvent = true
        items.forEach {
            it.isChecked = false
        }

        radioGroup.clearCheck()

        model.surveyAnswers[questionId]?.let { answers ->
            items.find {
                it.id == (answers.first() as SimpleAnswer).index
            }?.apply {
                isChecked = true
                lastRadioSelected = this.id
            }
        }

        if(items.all { !it.isChecked }) lastRadioSelected = -1

        validate(false)
        disableTriggeringEvent = false
    }

    override fun validate(save: Boolean): Boolean {
        val valid = lastRadioSelected != -1
        nextButton.isEnabled = valid
        if (valid && save) saveData()
        return valid
    }

    private fun saveData() {
        if (lastRadioSelected != -1) {
            viewModel.saveAnswers(questionId, listOf(SimpleAnswer(lastRadioSelected)))
        }
    }
}