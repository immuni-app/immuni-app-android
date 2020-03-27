package org.ascolto.onlus.geocrowd19.android.ui.log.fragment.types

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.models.survey.CompositeAnswer
import org.ascolto.onlus.geocrowd19.android.models.survey.PickerWidget
import org.ascolto.onlus.geocrowd19.android.models.survey.Survey
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.FormContentFragment
import org.ascolto.onlus.geocrowd19.android.ui.log.model.FormModel
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.visible
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.android.synthetic.main.form_picker_field.*
import kotlinx.android.synthetic.main.form_text_field.next


class PickerFieldFragment : FormContentFragment(R.layout.form_picker_field) {
    override val nextButton: Button
        get() = next
    override val prevButton: ImageView
        get() = back
    override val questionText: TextView
        get() = question
    override val descriptionText: TextView
        get() = description

    val items = mutableListOf<NumberPicker>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.survey.observe(viewLifecycleOwner, Observer {
            buildWidget(it)
        })

        nextButton.isEnabled = true
    }

    private fun buildWidget(it: Survey) {
        val question = it.questions.first { it.id == questionId }

        questionText.text = question.title
        descriptionText.text = question.description

        if (question.description.isEmpty()) descriptionText.gone()
        else descriptionText.visible()

        val widget = question.widget as PickerWidget
        widget.components.forEachIndexed { index, picker ->
            items.apply {
                add(NumberPicker(context).apply {
                    tag = index
                    val data = picker.toTypedArray()
                    minValue = 0
                    maxValue = minValue + data.size - 1
                    displayedValues = data
                    value = minValue
                    setOnValueChangedListener { picker, oldVal, newVal ->
                        validate()
                    }
                    dividerColor = resources.getColor(R.color.picker_divider_color)
                    textColor = Color.parseColor("#495D74")
                    selectedTextColor = Color.parseColor("#495D74")
                })
            }
        }

        pickerGroup.apply {
            items.forEach { addView(it) }
        }

        formModel()?.let {
            onFormModelUpdate(it)
        }
    }

    override fun onFormModelUpdate(model: FormModel) {
        model.surveyAnswers[questionId]?.let { answers ->
            val answer = answers.first() as CompositeAnswer
            answer.componentIndexes.forEachIndexed { pickerIndex, valueIndex ->
                if (items.size > pickerIndex) {
                    items[pickerIndex].value = valueIndex
                }
            }
        }
    }

    override fun validate(): Boolean {
        val isValid = true
        nextButton.isEnabled = isValid
        if (isValid) saveData()
        return isValid
    }

    private fun saveData() {
        val indexes = items.map { it.value }
        viewModel.saveAnswers(questionId, listOf(CompositeAnswer(indexes)))
    }
}
