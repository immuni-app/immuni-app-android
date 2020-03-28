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
import org.ascolto.onlus.geocrowd19.android.ui.log.LogViewModel
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.log_resume_fragment.*
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FormResumeFragment : Fragment() {

    private lateinit var viewModel: LogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.log_resume_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        with(resumeList) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = ResumeListAdapter()
        }

        viewModel.resumeModel.observe(viewLifecycleOwner, Observer { listModel ->
            (resumeList.adapter as ResumeListAdapter).update(listModel)
        })

        close.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.survey_exit_title))
                .setMessage(getString(R.string.survey_exit_message))
                .setPositiveButton(getString(R.string.exit)) { d, _ -> activity?.finish() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .setOnCancelListener { }
                .show()
        }

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        next.setOnClickListener {
            val action = FormFragmentDirections.actionGlobalFormDone()
            findNavController().navigate(action)
        }

        viewModel.onResumeRequested()
    }

    private fun goToMainActivity() {
        activity?.finish()
    }
}
