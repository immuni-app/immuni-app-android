package org.ascolto.onlus.geocrowd19.android.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.models.survey.CompositeAnswer
import org.ascolto.onlus.geocrowd19.android.models.survey.MultipleChoicesWidget
import org.ascolto.onlus.geocrowd19.android.models.survey.SimpleAnswer
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.FormContentFragment
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
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
    private var minimumAnswer = 1
    private var maximumAnswer = 10000

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.survey.observe(viewLifecycleOwner, Observer {
            buildWidget(it)
        })
    }

    private fun buildWidget(it: Survey) {
        val question = it.questions.first { it.id == questionId }
        val widget = question.widget as MultipleChoicesWidget

        questionText.text = question.title
        descriptionText.text = question.description

        minimumAnswer = widget.minNumberOfAnswers
        maximumAnswer = widget.maxNumberOfAnswers

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

        formModel()?.let {
            onFormModelUpdate(it)
        }
    }

    override fun onFormModelUpdate(model: FormModel) {
        model.surveyAnswers[questionId]?.let { answers ->
            for (answer in answers) {
                items.find {
                    (it.tag as Int) == (answer as SimpleAnswer).index
                }?.isChecked = true
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        validate()
    }

    override fun validate(): Boolean {
        val count = items.count { it.isChecked }
        var isValid = count > 0

        isValid = isValid && count >= minimumAnswer && count <= maximumAnswer

        nextButton.isEnabled = isValid
        if (isValid) saveData()
        return isValid
    }

    private fun saveData() {
        val indexes = items.filter { it.isChecked }.map { SimpleAnswer(it.tag as Int) }
        viewModel.saveAnswers(questionId, indexes)
    }
}
