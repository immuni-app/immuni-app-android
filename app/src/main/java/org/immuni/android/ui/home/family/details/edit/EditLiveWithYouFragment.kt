package org.immuni.android.ui.home.family.details.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.user_edit_livewithyou_activity.*
import kotlinx.android.synthetic.main.user_edit_livewithyou_activity.back
import org.immuni.android.ImmuniActivity
import org.immuni.android.R
import org.immuni.android.loading
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class EditLiveWithYouFragment : BaseEditFragment() {

    val args by navArgs<EditLiveWithYouFragmentArgs>()

    private lateinit var viewModel: EditDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_edit_livewithyou_activity, container, false)
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
            when(it.isInSameHouse) {
                true -> {
                    yes.isChecked = true
                    no.isChecked = false
                }
                false -> {
                    no.isChecked = true
                    yes.isChecked = false
                }
            }

            pageTitle.text = String.format(requireContext().getString(R.string.user_edit_live_with_you_title),
                it.nickname!!.humanReadable(requireContext(), it.gender))
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it)
        })

        back.setOnClickListener { findNavController().popBackStack() }

        update.setOnClickListener {
            val sameHouse = when {
                yes.isChecked -> true
                else -> false
            }

            val user = viewModel.user()
            user?.let {
                viewModel.updateUser(user.copy(isInSameHouse = sameHouse))
            }
        }
    }
}
