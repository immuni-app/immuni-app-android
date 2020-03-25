package org.ascolto.onlus.geocrowd19.android.ui.log.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.ui.log.LogViewModel
import com.bendingspoons.base.extensions.gone
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.visible
import kotlinx.android.synthetic.main.log_choose_person_fragment.*
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
                age.text = it.ageGroup.humanReadable(requireContext())
                name.text = if (it.isMain) getString(R.string.you) else it.name
                gender.setImageResource(
                    when (it.gender) {
                        Gender.FEMALE -> R.drawable.ic_avatar_female_purple
                        Gender.MALE -> R.drawable.ic_avatar_female_purple
                    }
                )

                when (it.isMain) {
                    true -> {
                        compileFor.gone()
                        compileFor.text = getString(R.string.choose_person_bottom_message)
                    }
                    false -> {
                        compileFor.visible()
                        compileFor.text = getString(R.string.choose_person_bottom_message_members)
                    }
                }
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