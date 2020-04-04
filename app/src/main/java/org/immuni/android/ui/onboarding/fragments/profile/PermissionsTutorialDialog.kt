package org.immuni.android.ui.onboarding.fragments.profile

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import org.immuni.android.R
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

open class PermissionsTutorialDialog : FullScreenDialogDarkFragment() {
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.onFinishPermissionsTutorial()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.onboarding_permissions_tutorial_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.setOnClickListener {
            dismiss()
        }
    }
}