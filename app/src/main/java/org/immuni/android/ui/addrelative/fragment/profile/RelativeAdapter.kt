package org.immuni.android.ui.addrelative.fragment.profile

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class RelativeAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var items: MutableList<Class<out RelativeContentFragment>> = mutableListOf(AgeRangeFragment::class.java)

    override fun getItemCount(): Int = items.size

    fun addPage(fragment: Class<out RelativeContentFragment>) {
        items.add(fragment)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun createFragment(position: Int): Fragment {
        return items[position].newInstance().apply {
            arguments = bundleOf("position" to position)
        }
    }
}
