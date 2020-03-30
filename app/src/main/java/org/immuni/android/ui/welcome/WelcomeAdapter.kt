package org.immuni.android.ui.welcome

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WelcomeAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return WelcomeItemFragment().apply {
            arguments = Bundle().apply {
                putInt("position", position)
            }
        }
    }
}
