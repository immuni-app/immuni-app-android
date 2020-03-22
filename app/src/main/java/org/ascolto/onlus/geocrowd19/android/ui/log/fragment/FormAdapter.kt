package org.ascolto.onlus.geocrowd19.android.ui.log.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.ascolto.onlus.geocrowd19.android.models.survey.*
import org.ascolto.onlus.geocrowd19.android.ui.log.fragment.types.*

class FormAdapter(
    val survey: Survey,
    private val firstQuestion: QuestionId,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    // initialize with only the first question
    private var questionIds: MutableList<String> = mutableListOf(firstQuestion)

    fun addNextWidget(questionId: String) {
        val index = questionIds.indexOf(questionId)
        if (index != -1) {
            questionIds = questionIds.subList(0, index)
        }
        questionIds.add(questionId)
    }

    override fun getItemCount(): Int = questionIds.size

    override fun createFragment(position: Int): Fragment {
        val question = survey.questions.first { it.id == questionIds[position] }

        return when (question.widget) {
            is PickerWidget -> PickerFieldFragment()
            is MultipleChoicesWidget -> MultipleChoiceFieldFragment()
            is RadioWidget -> RadioFieldFragment()
        }.apply {
            arguments = bundleOf(
                "position" to position,
                "questionId" to question.id
            )
        }
    }
}