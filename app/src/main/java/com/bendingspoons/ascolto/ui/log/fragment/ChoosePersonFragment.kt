package com.bendingspoons.ascolto.ui.log.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.db.entity.Gender
import com.bendingspoons.ascolto.db.entity.age
import com.bendingspoons.ascolto.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
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

        viewModel.mainUserInfo.observe(viewLifecycleOwner, Observer {
            it?.let {
                age.text = it.age().toString()
                name.text = when(it.isMainUser) {
                    true -> getString(R.string.you)
                    false -> it.name
                }
                gender.setImageResource(when(it.gender) {
                    Gender.FEMALE -> R.drawable.ic_avatar_female_purple
                    Gender.MALE -> R.drawable.ic_avatar_female_purple
                })
            }
        })

        next.setOnClickListener {
            val action = ChoosePersonFragmentDirections.actionGlobalForm()
            findNavController().navigate(action)
        }

        back.setOnClickListener {
            activity?.finish()
        }
    }
}