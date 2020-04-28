package org.immuni.android.ui.home.family.details.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.user_edit_gender_activity.*
import org.immuni.android.R
import org.immuni.android.models.Gender
import com.bendingspoons.base.extensions.loading
import org.immuni.android.util.ProgressDialogFragment
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class EditGenderFragment : BaseEditFragment() {

    val args by navArgs<EditGenderFragmentArgs>()

    private lateinit var viewModel: EditDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_edit_gender_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel { parametersOf(args.userId)}

        viewModel.navigateBack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().popBackStack()
            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            when(it.gender) {
                Gender.FEMALE -> {
                    female.isChecked = true
                    male.isChecked = false
                }
                Gender.MALE -> {
                    male.isChecked = true
                    female.isChecked = false
                }
            }

            pageTitle.text = when(it.isMain) {
                true -> requireContext().resources.getString(R.string.onboarding_gender_title)
                false -> requireContext().resources.getString(R.string.user_edit_gender_you_title)
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it, ProgressDialogFragment())
        })

        back.setOnClickListener { findNavController().popBackStack() }

        update.setOnClickListener {
            val gender = when {
                female.isChecked -> Gender.FEMALE
                else -> Gender.MALE
            }

            val user = viewModel.user()
            user?.let {
                viewModel.updateUser(user.copy(gender = gender))
            }
        }
    }
}
