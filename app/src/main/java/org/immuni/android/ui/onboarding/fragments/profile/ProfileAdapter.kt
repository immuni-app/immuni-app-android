package org.immuni.android.ui.onboarding.fragments.profile

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.immuni.android.managers.UserManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class ProfileAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), KoinComponent {

    private val userManager: UserManager by inject()

    private var items: List<Type> = userManager.mainUser()?.let {
        listOf(
            Type.BT_INTRO,
            Type.PERMISSIONS
        )
    } ?: listOf(
        Type.AGE_RANGE,
        Type.GENDER,
        Type.BT_INTRO,
        Type.PERMISSIONS
    )

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val fragment = when (items[position]) {
            Type.BT_INTRO -> BluetoothIntroFragment()
            Type.PERMISSIONS -> PermissionsFragment()
            Type.AGE_RANGE -> AgeRangeFragment()
            Type.GENDER -> GenderFragment()
        }

        return fragment.apply {
            arguments = bundleOf("position" to position)
        }
    }

    private enum class Type {
        BT_INTRO, PERMISSIONS, AGE_RANGE, GENDER
    }
}
