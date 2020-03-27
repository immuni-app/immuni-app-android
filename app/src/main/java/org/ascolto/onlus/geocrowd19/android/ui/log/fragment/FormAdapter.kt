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
    var questionIds: MutableList<String> = mutableListOf(firstQuestion)

    fun updateAdapter(questions: Collection<String>) {
        questionIds.clear()
        questionIds.addAll(questions)
    }

    // IMPORTANT: this is critically important to allow the viewpager to instantiate the correct
    // fragment instance

    override fun getItemId(position: Int): Long {
        return survey.questions.indexOfFirst { it.id == questionIds[position] }.toLong()
    }

    // IMPORTANT: this is critically important in combination with getItemId to restore
    // the adapter after activity restoration
    override fun containsItem(itemId: Long): Boolean {
        val surveyQuestion = survey.questions[itemId.toInt()]
        return questionIds.contains(surveyQuestion.id)
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