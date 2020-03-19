package com.bendingspoons.ascolto.ui.onboarding.fragments.profile

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfileAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var items: List<Class<out ProfileContentFragment>> = listOf(
        NameFragment::class.java,
        AgeFragment::class.java,
        GenderFragment::class.java
    )

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return items[position].newInstance().apply {
            arguments = bundleOf("position" to position)
        }
    }
}
