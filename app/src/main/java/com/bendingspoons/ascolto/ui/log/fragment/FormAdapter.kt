package com.bendingspoons.ascolto.ui.log.fragment

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bendingspoons.ascolto.ui.log.fragment.types.TextFieldFragment

class FormAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var items: List<Class<out FormContentFragment>> = listOf(
        TextFieldFragment::class.java,
        TextFieldFragment::class.java
    )

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return items[position].newInstance()
    }
}