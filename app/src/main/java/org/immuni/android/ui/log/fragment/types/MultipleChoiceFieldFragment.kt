package org.immuni.android.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import org.immuni.android.R
import org.immuni.android.models.survey.MultipleChoicesWidget
import org.immuni.android.models.survey.SimpleAnswer
import org.immuni.android.models.survey.Survey
import org.immuni.android.ui.log.fragment.FormContentFragment
import org.immuni.android.ui.log.model.FormModel
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.ScreenUtils
import kotlinx.android.synthetic.main.form_multiple_choice_field.*

class MultipleChoiceFieldFragment : FormContentFragment(R.layout.form_multiple_choice_field),
    CompoundButton.OnCheckedChangeListener {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val questionText: TextView
        get() = question
    override val descriptionText: TextView
        get() = description

    private val items = mutableListOf<CheckBox>()
    private var minimumAnswers = 0
    private var maximumAnswers = 10000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.survey.observe(viewLifecycleOwner, Observer {
            buildWidget(it)
        })
    }

    private fun buildWidget(it: Survey) {
        items.clear()
        val question = it.questions.first { it.id == questionId }
        val widget = question.widget as MultipleChoicesWidget

        questionText.text = question.title
        descriptionText.text = question.description

        minimumAnswers = widget.minNumberOfAnswers
        maximumAnswers = widget.maxNumberOfAnswers

        if (question.description.isEmpty()) descriptionText.gone()
        else descriptionText.visible()


        widget.answers.forEachIndexed { index, answer ->
            items.apply {
                add(CheckBox(context).apply {
                    tag = index
                    text = answer
                    val tf = ResourcesCompat.getFont(context, R.font.euclid_circular_bold)
                    typeface = tf
                    textSize = 18f
                    buttonDrawable = ContextCompat.getDrawable(context, R.drawable.checkbox)
                    val paddingLeft = ScreenUtils.convertDpToPixels(context, 20)
                    val paddingTop = ScreenUtils.convertDpToPixels(context, 16)
                    val paddingBottom = ScreenUtils.convertDpToPixels(context, 16)
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
                    setTextColor(Color.parseColor("#495D74"))
                    setOnCheckedChangeListener(this@MultipleChoiceFieldFragment)
                })
            }
        }

        multipleChoiceGroup.apply {
            items.forEach {
                addView(it)
            }
        }

        validate(false)

        formModel()?.let {
            onFormModelUpdate(it)
        }
    }

    override fun onFormModelUpdate(model: FormModel) {
        disableTriggeringEvent = true
        items.forEach {
            it.isChecked = false
        }
        model.surveyAnswers[questionId]?.let { answers ->
            for (answer in answers) {
                val checkbox = items.find {
                    (it.tag as Int) == (answer as SimpleAnswer).index
                }
                checkbox?.apply {
                    isChecked = true
                }
            }
        }
        validate(false)
        disableTriggeringEvent = false
    }

    var disableTriggeringEvent = false
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(!disableTriggeringEvent) validate()
    }

    override fun validate(save: Boolean): Boolean {
        val count = items.count { it.isChecked }
        val isValid = count in minimumAnswers..maximumAnswers

        nextButton.isEnabled = isValid
        if (isValid && save) saveData()
        return isValid
    }

    private fun saveData() {
        val indexes = items.filter { it.isChecked }.map { SimpleAnswer(it.tag as Int) }
        viewModel.saveAnswers(questionId, indexes)
    }
}
