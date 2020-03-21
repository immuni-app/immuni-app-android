package com.bendingspoons.ascolto.ui.log.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bendingspoons.ascolto.models.survey.*
import com.bendingspoons.ascolto.ui.log.fragment.types.*

class FormAdapter(val survey: Survey?, fragment: Fragment) : FragmentStateAdapter(fragment) {

    /*
    var items: MutableList<Class<out FormContentFragment>> = (survey?.questions ?: listOf()).map { when(it.widget) {
        is PickerWidget -> PickerFieldFragment::class.java
        is MultipleChoicesWidget -> MultipleChoiceFieldFragment::class.java
        is RadioWidget -> RadioFieldFragment::class.java
    } }.toMutableList()

     */

    // initialize with only the first question
    var questionIds: MutableList<String> = mutableListOf(survey?.questions?.map{ it.id }?.first()!!)

    fun addNextWidget(questionId: String) {
        val index = questionIds.indexOf(questionId)
        if(index != -1) {
            questionIds = questionIds.subList(0, index)
        }
        questionIds.apply {
            add(questionId)
        }
    }

    override fun getItemCount(): Int = questionIds.size

    override fun createFragment(position: Int): Fragment {

        val question = (survey?.questions ?: listOf()).find { it.id == questionIds[position] }!!

        return when(question.widget) {
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