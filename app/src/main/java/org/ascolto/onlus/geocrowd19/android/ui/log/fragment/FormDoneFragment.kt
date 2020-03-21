package org.ascolto.onlus.geocrowd19.android.ui.log.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeActivity
import org.ascolto.onlus.geocrowd19.android.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FormDoneFragment : Fragment() {

    private lateinit var viewModel: LogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // users must select a choice
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.log_done_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setDarkStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        viewModel.navigateToMainPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                goToMainActivity()
            }
        })

        viewModel.onLogComplete()
    }

    private fun goToMainActivity() {
        val intent = Intent(AscoltoApplication.appContext, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()
    }

}