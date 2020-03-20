package com.bendingspoons.ascolto.ui.log.fragment

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bendingspoons.ascolto.models.survey.*
import com.bendingspoons.ascolto.ui.log.fragment.types.*

class FormAdapter(val survey: Survey?, fragment: Fragment) : FragmentStateAdapter(fragment) {

    var items: List<Class<out FormContentFragment>> = (survey?.questions ?: listOf()).map { when(it.widget) {
        is PickerWidget -> PickerFieldFragment::class.java
        is MultipleChoicesWidget -> MultipleChoiceFieldFragment::class.java
        is RadioWidget -> RadioFieldFragment::class.java
    } }.toMutableList().apply {
        add(0, SomethingChangedFieldFragment::class.java)
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return items[position].newInstance().apply {
            arguments = bundleOf(
                "position" to position,
                "questionId" to (survey?.questions?.get((position-1).coerceAtLeast(0))?.id ?: "")
            )
        }
    }
}