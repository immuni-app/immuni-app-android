package com.bendingspoons.ascolto.ui.home.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bendingspoons.ascolto.AscoltoApplication
import com.bendingspoons.ascolto.OnboardingDirections
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.ui.home.HomeActivity
import com.bendingspoons.ascolto.ui.home.HomeSharedViewModel
import com.bendingspoons.ascolto.ui.log.LogActivity
import com.bendingspoons.ascolto.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.log_choose_person_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class HomeFragment : Fragment() {

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
        return inflater.inflate(R.layout.home_fragment, container, false)
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
        */

        next.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, LogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity?.startActivity(intent)
        }
    }
}