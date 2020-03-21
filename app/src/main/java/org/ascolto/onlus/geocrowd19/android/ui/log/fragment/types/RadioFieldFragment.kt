package org.ascolto.onlus.geocrowd19.android.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.CompoundButton.*
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
                    tag = index
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

        formModel()?.let {
            onFormModelUpdate(it)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        validate()
    }

    override fun onFormModelUpdate(model: FormModel) {
        model.answers[questionId]?.let { answer ->
            items.find { (it.tag as Int) == (answer as SimpleAnswer).index }?.isChecked = true
        }
    }

    override fun validate(): Boolean {
        val valid = items.any { it.isChecked }
        nextButton.isEnabled = valid
        if(valid) saveData()
        return valid
    }

    private fun saveData() {
        val indexes = items.filter { it.isChecked }.mapNotNull { it.tag as? Int }
        viewModel.saveAnswer(questionId, SimpleAnswer(indexes[0]))
    }
}