package it.ministerodellasalute.immuni.ui.greencertificate.tabadapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdapter(
    fm: Fragment,
    var totalTabs: Int
) :
    FragmentStateAdapter(fm) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TabActive()
            1 -> TabExpired()
            else -> createFragment(position)
        }
    }

    override fun getItemCount(): Int {
        return totalTabs
    }

//    override fun getItem(position: Int): Fragment {
//        var f: Fragment? = null
//        when (position) {
//            0 -> {
//                val tab1 = TabActive()
//                f = tab1
//            }
//            1 -> {
//                val tab2 = TabExpired()
//                f = tab2
//            }
//        }
//        return f!!
//    }

    fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Active"
            1 -> "Expired"
            else -> getPageTitle(position)
        }
    }
//
//    override fun getCount(): Int {
//        return totalTabs
//    }
}
