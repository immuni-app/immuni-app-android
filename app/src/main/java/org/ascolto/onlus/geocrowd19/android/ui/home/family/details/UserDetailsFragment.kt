package org.ascolto.onlus.geocrowd19.android.ui.home.family.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.bendingspoons.base.extensions.visible
import com.bendingspoons.base.utils.DeviceUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.user_details_fragment.*
import kotlinx.android.synthetic.main.user_details_fragment.identifier
import kotlinx.android.synthetic.main.user_details_fragment.name
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.loading
import org.ascolto.onlus.geocrowd19.android.models.Nickname
import org.ascolto.onlus.geocrowd19.android.toast
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class UserDetailsFragment : Fragment() {

    private val fragmentArgs: UserDetailsFragmentArgs by navArgs()
    private lateinit var viewModel: UserDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requireActivity().onBackPressedDispatcher.addCallback(this) {
        // users must select a choice
        //}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getViewModel { parametersOf(fragmentArgs.userId) }
        return inflater.inflate(R.layout.user_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            activity?.loading(it)
        })

        viewModel.navigateBack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().popBackStack()
            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            pageTitle.text = when (it.isMain) {
                true -> requireContext().resources.getString(R.string.you)
                false -> it.name
            }
            val ctx = requireContext()
            name.text = when (it.isMain) {
                true -> requireContext().resources.getString(R.string.you)
                false -> it.nickname?.humanReadable(ctx, it.gender)
                    ?: ctx.getString(R.string.nickname_not_specified)
            }
            age.text = it.ageGroup.id
            sex.text = when (it.gender) {
                Gender.MALE -> requireContext().resources.getString(R.string.onboarding_male)
                Gender.FEMALE -> requireContext().resources.getString(R.string.onboarding_female)
            }

            liveWithYou.text = when (it.isInSameHouse) {
                true -> requireContext().resources.getString(R.string.yes)
                false -> requireContext().resources.getString(R.string.no)
                else -> "-"
            }

            identifier.text = it.id

            // main user cannot be deleted

            if (it.isMain) delete.gone()
            else delete.visible()

        })

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        copyIdentifier.setOnClickListener {
            DeviceUtils.copyToClipBoard(AscoltoApplication.appContext, text = viewModel.userId)
            toast(requireContext().resources.getString(R.string.user_id_copied))
        }

        editName.setOnClickListener {
            toast("Edit name")
        }

        editAge.setOnClickListener {
            toast("Edit age")
        }

        editSex.setOnClickListener {
            toast("Edit sex")
        }

        editLiveWithYou.setOnClickListener {
            toast("Edit live with you")
        }

        delete.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.delete_user_alert_title))
                .setMessage(getString(R.string.delete_user_alert_message))
                .setPositiveButton(getString(R.string.delete)) { d, _ -> viewModel.deleteUser()}
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss()}
                .setOnCancelListener {  }
                .show()
        }
    }
}