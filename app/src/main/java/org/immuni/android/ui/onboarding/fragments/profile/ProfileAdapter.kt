package org.immuni.android.ui.onboarding.fragments.profile

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bendingspoons.oracle.Oracle
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.koin.core.KoinComponent
import org.koin.core.inject

class ProfileAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), KoinComponent {

    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    var items: List<Class<out ProfileContentFragment>> = oracle.me()?.mainUser?.let {
        listOf(
            BluetoothIntroFragment::class.java,
            PermissionsFragment::class.java
        )
    } ?: listOf(
        AgeRangeFragment::class.java,
        GenderFragment::class.java,
        BluetoothIntroFragment::class.java,
        PermissionsFragment::class.java
    )

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return items[position].newInstance().apply {
            arguments = bundleOf("position" to position)
        }
    }
}
