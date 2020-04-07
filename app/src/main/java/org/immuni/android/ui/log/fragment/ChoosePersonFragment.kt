package org.immuni.android.ui.log.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bendingspoons.base.extensions.animateShow
import org.immuni.android.R
import org.immuni.android.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import kotlinx.android.synthetic.main.log_choose_person_fragment.*
import org.immuni.android.models.Gender
import org.immuni.android.models.colorResource
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class ChoosePersonFragment : Fragment() {

    private lateinit var viewModel: LogViewModel

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
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.log_choose_person_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setDarkStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {
                clinicDiaryForUser.text =
                    if (it.isMain) Html.fromHtml(getString(R.string.your_clinic_diary))
                    else Html.fromHtml(getString(R.string.clinic_diary_of, it.name))

                val themeColor = ContextCompat.getColor(
                    requireContext(),
                    colorResource(
                        viewModel.deviceId,
                        viewModel.userIndex!!
                    )
                )
                backgroundLayout.setBackgroundColor(themeColor)
                backgroundLayout.animateShow()
                next.setTextColor(themeColor)

                icon.setImageResource(
                    if (it.gender == Gender.FEMALE) R.drawable.ic_avatar_white_female
                    else R.drawable.ic_avatar_white_male
                )

                bottomMessage.text = getString(
                    if (it.isMain) R.string.choose_person_bottom_message
                    else R.string.choose_person_bottom_message_members
                )
            }
        })

        next.setOnClickListener {
            viewModel.reset()
            val action = ChoosePersonFragmentDirections.actionGlobalForm()
            findNavController().navigate(action)
        }

        back.setOnClickListener {
            activity?.finish()
        }
    }
}