package org.ascolto.onlus.geocrowd19.android.ui.onboarding.fragments.profile

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bendingspoons.oracle.Oracle
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.koin.core.KoinComponent
import org.koin.core.inject

class ProfileAdapter(fragment: Fragment) : FragmentStateAdapter(fragment), KoinComponent {

    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()

    var items: List<Class<out ProfileContentFragment>> = oracle.me()?.mainUser?.let {
        listOf(
            BluetoothPermissionsFragment::class.java
        )
    } ?: listOf(
        //AgeFragment::class.java,
        AgeRangeFragment::class.java,
        GenderFragment::class.java,
        BluetoothPermissionsFragment::class.java
    )

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return items[position].newInstance().apply {
            arguments = bundleOf("position" to position)
        }
    }
}
