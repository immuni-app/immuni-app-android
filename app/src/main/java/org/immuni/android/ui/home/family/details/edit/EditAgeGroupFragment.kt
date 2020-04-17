package org.immuni.android.ui.home.family.details.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bendingspoons.base.extensions.gone
import kotlinx.android.synthetic.main.user_edit_age_group_activity.*
import org.immuni.android.R
import org.immuni.android.loading
import org.immuni.android.models.AgeGroup
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class EditAgeGroupFragment : BaseEditFragment() {

    val args by navArgs<EditAgeGroupFragmentArgs>()

    private lateinit var viewModel: EditDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_edit_age_group_activity, container, false)
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

            if(it.isMain) {
                age_range_0_13.gone()
                age_range_14_17.gone()
            }

            age_range_0_13.isChecked = false
            age_range_14_17.isChecked = false
            age_range_18_35.isChecked = false
            age_range_36_45.isChecked = false
            age_range_46_55.isChecked = false
            age_range_56_65.isChecked = false
            age_range_66_75.isChecked = false
            age_range_75.isChecked = false

            when(it.ageGroup) {
                AgeGroup.ZERO_THIRTEEN -> age_range_0_13.isChecked = true
                AgeGroup.FOURTEEN_SEVENTEEN -> age_range_14_17.isChecked = true
                AgeGroup.EIGHTEEN_THIRTYFIVE -> age_range_18_35.isChecked = true
                AgeGroup.THRITYSIX_FORTYFIVE -> age_range_36_45.isChecked = true
                AgeGroup.FORTYSIX_FIFTYFIVE -> age_range_46_55.isChecked = true
                AgeGroup.FIFTYSIX_SIXTYFIVE -> age_range_56_65.isChecked = true
                AgeGroup.SIXTYSIX_SEVENTYFIVE -> age_range_66_75.isChecked = true
                AgeGroup.MORE_THAN_SEVENTYFIVE -> age_range_75.isChecked = true
            }

            pageTitle.text = when(it.isMain) {
                true -> requireContext().getString(R.string.onboarding_age_title)
                false -> String.format(requireContext().getString(R.string.user_edit_age_you_title),
                    it.nickname!!.humanReadable(requireContext(), it.gender))
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it)
        })

        back.setOnClickListener { findNavController().popBackStack() }

        update.setOnClickListener {
            val ageGroup = when {
                age_range_0_13.isChecked -> AgeGroup.ZERO_THIRTEEN
                age_range_14_17.isChecked -> AgeGroup.FOURTEEN_SEVENTEEN
                age_range_18_35.isChecked -> AgeGroup.EIGHTEEN_THIRTYFIVE
                age_range_36_45.isChecked -> AgeGroup.THRITYSIX_FORTYFIVE
                age_range_46_55.isChecked -> AgeGroup.FORTYSIX_FIFTYFIVE
                age_range_56_65.isChecked -> AgeGroup.FIFTYSIX_SIXTYFIVE
                age_range_66_75.isChecked -> AgeGroup.SIXTYSIX_SEVENTYFIVE
                else -> AgeGroup.MORE_THAN_SEVENTYFIVE
            }

            val user = viewModel.user()
            user?.let {
                viewModel.updateUser(user.copy(ageGroup = ageGroup))
            }
        }
    }
}
