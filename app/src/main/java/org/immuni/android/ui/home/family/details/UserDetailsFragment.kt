package org.immuni.android.ui.home.family.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bendingspoons.base.extensions.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.user_details_fragment.*
import kotlinx.android.synthetic.main.user_details_fragment.name
import org.immuni.android.R
import org.immuni.android.db.entity.Gender
import org.immuni.android.loading
import org.immuni.android.ui.home.family.details.edit.*
import org.immuni.android.ui.uploadData.UploadDataActivity
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
            age.text = it.ageGroup.humanReadable(ctx)
            sex.text = when (it.gender) {
                Gender.MALE -> requireContext().resources.getString(R.string.onboarding_male)
                Gender.FEMALE -> requireContext().resources.getString(R.string.onboarding_female)
            }

            if(it.isMain) {
                liveWithYouGroup.gone()
                nicknameGroup.gone()
            } else {
                liveWithYouGroup.visible()
                nicknameGroup.visible()
                liveWithYou.text = when (it.isInSameHouse) {
                    true -> requireContext().resources.getString(R.string.yes)
                    false -> requireContext().resources.getString(R.string.no)
                    else -> "-"
                }
            }

            //identifier.text = it.id

            // main user cannot be deleted

            if (it.isMain) delete.gone()
            else delete.visible()

        })

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        uploadButton.setOnClickListener {
            navigateToUploadData()
        }

        editName.setOnClickListener {
            navigateToEditName()
        }

        editAge.setOnClickListener {
            navigateToEditAgeGroup()
        }

        editSex.setOnClickListener {
            navigateToEditGender()
        }

        editLiveWithYou.setOnClickListener {
            navigateToEditLiveWithYou()
        }

        delete.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.delete_user_alert_title))
                .setMessage(getString(R.string.delete_user_alert_message))
                .setPositiveButton(getString(R.string.delete)) { d, _ -> viewModel.deleteUser() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .setOnCancelListener {  }
                .show()
        }
    }

    private fun navigateToEditAgeGroup() {
        val intent = Intent(requireContext(), EditAgeGroupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("userId", fragmentArgs.userId)
        }
        activity?.startActivity(intent)
    }

    private fun navigateToEditName() {
        val intent = Intent(requireContext(), EditNicknameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("userId", fragmentArgs.userId)
        }
        activity?.startActivity(intent)
    }

    private fun navigateToEditGender() {
        val intent = Intent(requireContext(), EditGenderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("userId", fragmentArgs.userId)
        }
        activity?.startActivity(intent)
    }

    private fun navigateToEditLiveWithYou() {
        val intent = Intent(requireContext(), EditLiveWithYouActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("userId", fragmentArgs.userId)
        }
        activity?.startActivity(intent)
    }

    private fun navigateToUploadData() {
        val intent = Intent(requireContext(), UploadDataActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("userId", fragmentArgs.userId)
        }
        activity?.startActivity(intent)
    }

    private fun showUploadDataMessage() {
        MaterialAlertDialogBuilder(context)
            .setTitle("Caricare i dati?")
            .setMessage("I tuoi dati verranno caricati sui nostri server in modo anonimo per consentirci di contattare tutte le persone che sono state a contatto con te negli ultimi giorni.")
            .setPositiveButton(getString(R.string.upload)) { d, _ -> d.dismiss()}
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss()}
            .setOnCancelListener {  }
            .show()
    }
}