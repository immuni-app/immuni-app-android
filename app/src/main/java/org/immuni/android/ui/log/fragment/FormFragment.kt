package org.immuni.android.ui.log.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.log_form_fragment.*
import org.immuni.android.R
import org.immuni.android.toast
import org.immuni.android.ui.log.LogViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class FormFragment : Fragment() {

    private lateinit var viewModel: LogViewModel
    private lateinit var pageChangeCallback: ViewPager2.OnPageChangeCallback
    lateinit var listAdapter: FormAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = getSharedViewModel()
        return inflater.inflate(R.layout.log_form_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        progress.clipToOutline = true

        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                progress.setPercentage(viewModel.getProgressPercentage(viewPager.currentItem))
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
        }

        listAdapter = FormAdapter(
            viewModel.survey.value!!,
            viewModel.formModel.value!!.currentQuestion,
            this@FormFragment
        )
        listAdapter.updateAdapter(viewModel.formModel()!!.answeredQuestions)
        with(viewPager) {
            adapter = listAdapter
            clipToPadding = false
            clipChildren = false
            isUserInputEnabled = false
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeCallback)
        }

        close.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.survey_exit_title))
                .setMessage(getString(R.string.survey_exit_message))
                .setPositiveButton(getString(R.string.exit)) { d, _ -> activity?.finish() }
                .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
                .setOnCancelListener { }
                .show()
        }

        viewModel.navigateToNextPage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                toast("ON NEXT PAGE GO")
                val newPos = viewPager.currentItem + 1

                if (newPos == (viewPager.adapter?.itemCount ?: 0)) {
                    val action = FormFragmentDirections.actionGlobalFormDone()
                    findNavController().navigate(action)
                } else {
                    viewPager.setCurrentItem(newPos, true)
                }
            }
        })

        viewModel.navigateToDonePage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val action = FormFragmentDirections.actionGlobalFormDone()
                findNavController().navigate(action)
            }
        })

        viewModel.navigateToResume.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                val action = FormFragmentDirections.actionGlobalFormResume()
                findNavController().navigate(action)
            }
        })

        viewModel.navigateToQuestion.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { nextQuestion ->
                // add the new fragment to adapter
                listAdapter.updateAdapter(viewModel.formModel()!!.answeredQuestions)
                listAdapter.notifyDataSetChanged()

                // navigate there
                val newPos = viewPager.currentItem + 1
                viewPager.setCurrentItem(newPos, true)
            }
        })

        viewModel.navigateToPrevPage.observe(viewLifecycleOwner, Observer {

            // add the new fragment to adapter
            //listAdapter.addPrevWidget("question$i")
            //listAdapter.notifyDataSetChanged()

            it.getContentIfNotHandled()?.let {
                onBackPressed()
            }
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let {
                progressText.text = if (it.isMain) {
                    getString(R.string.your_clinic_diary_form)
                } else {
                    String.format(getString(R.string.clinic_diary_of_form), it.name)
                }
            }
        })
    }

    private fun onBackPressed() {
        val newPos = viewPager.currentItem - 1
        if (newPos >= 0) {
            viewPager.setCurrentItem(newPos, true)
        } else {
            findNavController().popBackStack()
        }
    }

    // Uncomment this if we want to restart the survey after the system kill the app
    /*
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        // avoid using outdated settings survey, or even worse
        // misaligned with the current survey answers

        if(savedInstanceState != null) activity?.finish()
    }
     */
}
