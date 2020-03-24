package org.ascolto.onlus.geocrowd19.android.ui.home.family

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.family_fragment.*
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.AddFamilyMemberButtonCard
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.AddFamilyMemberTutorialCard
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.FamilyItemType
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.UserCard
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FamilyFragment : Fragment(R.layout.family_fragment), FamilyClickListener {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        // Fade out toolbar on scroll
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = kotlin.math.abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            pageTitle?.alpha = 1f - ratio
        })

        with(familyList) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = FamilyListAdapter(this@FamilyFragment)
        }

        viewModel.familylistModel.observe(viewLifecycleOwner, Observer {
            (familyList.adapter as? FamilyListAdapter)?.items?.apply {
                clear()
                addAll(it)
            }

            familyList.adapter?.notifyDataSetChanged()
        })
    }

    override fun onClick(item: FamilyItemType) {
        when(item) {
            is UserCard -> {
                if(item.userIdTapped) {
                    viewModel.onUserIdTap(item.user)
                } else {
                    val action = FamilyFragmentDirections.actionUserDetails(item.user.id)
                    findNavController().navigate(action)
                }
            }
            is AddFamilyMemberTutorialCard -> {
                //viewModel.openSuggestions(item.severity)
            }
            is AddFamilyMemberButtonCard -> {
                //viewModel.openSuggestions(item.severity)
            }
        }
    }
}