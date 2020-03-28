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
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import org.ascolto.onlus.geocrowd19.android.models.survey.TriageProfile
import org.ascolto.onlus.geocrowd19.android.models.survey.backgroundColor
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialogActivity.Companion.TRIAGE_DIALOG_RESULT
import org.ascolto.onlus.geocrowd19.android.ui.log.LogActivity
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
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        viewModel.navigateToNextLogStartPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                goToNextLogStart()
            }
        })

        viewModel.navigateToMainPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                goToMainActivity()
            }
        })

        viewModel.navigateToTriagePage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { profile ->
                openTriageDialog(profile)
            }
        })

        if(savedInstanceState == null) {
            viewModel.onLogComplete()
        }
    }

    private fun openTriageDialog(triageProfile: TriageProfile) {
        val context = AscoltoApplication.appContext
        val intent = Intent(context, WebViewDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("url", triageProfile.url)
            putExtra("color", triageProfile.severity.backgroundColor())
        }
        startActivityForResult(intent, TRIAGE_DIALOG_RESULT)
    }

    private fun goToNextLogStart() {
        val intent = Intent(AscoltoApplication.appContext, LogActivity::class.java)
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun goToMainActivity() {
        activity?.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == TRIAGE_DIALOG_RESULT) {
            viewModel.navigateToNextStep()
        }
    }
}
