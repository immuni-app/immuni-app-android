package org.ascolto.onlus.geocrowd19.android.ui.home.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import org.ascolto.onlus.geocrowd19.android.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.log_choose_person_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class SettingsFragment : Fragment() {

    private lateinit var viewModel: HomeSharedViewModel

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
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        /*
        viewModel.navigateToMainPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                goToMainActivity()
            }
        })


        next.setOnClickListener {
            val action = StartFragmentDirections.actionGlobalForm()
            findNavController().navigate(action)
        }*/
    }
}