package org.immuni.android.ui.home.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.addrelative.AddRelativeActivity
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment
import org.immuni.android.ui.home.HomeSharedViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel


class AddFamilyMemberDialog: FullScreenDialogLightFragment() {

    companion object {
        const val REQUEST_CODE_FAMILY_DIALOG = 101
    }

    private lateinit var viewModel: HomeSharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.family_member_add_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getSharedViewModel()

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        goAhead.setOnClickListener {
            findNavController().popBackStack()
        }

        addMember.setOnClickListener {
            openAddFamilyMemberActivity()
            findNavController().popBackStack()
            // avoid flickering
            GlobalScope.launch(Dispatchers.Main) {
                runCatching {
                    delay(1000)
                    viewModel.selectFamilyTab()
                }
            }
        }
    }

    private fun openAddFamilyMemberActivity() {
        val intent = Intent(ImmuniApplication.appContext, AddRelativeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
    }
}