package it.ministerodellasalute.immuni.ui.greencertificate.tabadapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.ministerodellasalute.immuni.R

class TabAdapter(
    val context: Context,
    fm: Fragment,
    var totalTabs: Int
) :
    FragmentStateAdapter(fm) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TabActive()
            else -> createFragment(position)
        }
    }

    override fun getItemCount(): Int {
        return totalTabs
    }

    fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.green_pass_active)
            else -> getPageTitle(position)
        }
    }
}
