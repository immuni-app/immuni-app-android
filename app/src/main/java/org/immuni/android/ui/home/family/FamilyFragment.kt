package org.immuni.android.ui.home.family

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.immuni.android.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.family_fragment.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.base.extensions.disableDragging
import org.immuni.android.ui.addrelative.AddRelativeActivity
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.home.family.model.AddFamilyMemberButtonCard
import org.immuni.android.ui.home.family.model.AddFamilyMemberTutorialCard
import org.immuni.android.ui.home.family.model.FamilyItemType
import org.immuni.android.ui.home.family.model.UserCard
import org.immuni.android.ui.uploaddata.UploadDataActivity
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FamilyFragment : Fragment(R.layout.family_fragment), FamilyClickListener {

    private lateinit var viewModel: HomeSharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        appBar.disableDragging()

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
                if (item.uploadTapped) {
                    navigateToUploadData(item.user.id)
                } else {
                    val action = FamilyFragmentDirections.actionUserDetails(item.user.id)
                    findNavController().navigate(action)
                }
            }
            is AddFamilyMemberTutorialCard -> {
                navigateToAddRelative()
            }
            is AddFamilyMemberButtonCard -> {
                navigateToAddRelative()
            }
        }
    }

    private fun navigateToUploadData(userId: String) {
        val intent = Intent(requireContext(), UploadDataActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("userId", userId)
        }
        activity?.startActivity(intent)
    }

    private fun navigateToAddRelative() {
        val intent = Intent(ImmuniApplication.appContext, AddRelativeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
    }
}