package org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import org.ascolto.onlus.geocrowd19.android.R
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import kotlinx.android.synthetic.main.add_relative_interrupt_fragment.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.AddRelativeActivity
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.AddRelativeViewModel
import org.koin.androidx.viewmodel.ext.android.getSharedViewModel

class InterruptFragment : Fragment() {
    private val navArg: InterruptFragmentArgs by navArgs()
    private lateinit var viewModel: AddRelativeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // prevent crashes for invalid state
        if(savedInstanceState != null) activity?.finish()

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
        return inflater.inflate(R.layout.add_relative_interrupt_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setDarkStatusBarFullscreen(resources.getColor(android.R.color.transparent))

        title.text = navArg.title
        message.text = navArg.message

        finish.setOnClickListener {
            activity?.finish()
        }

        addAnotherRelative.setOnClickListener {
            val intent = Intent(AscoltoApplication.appContext, AddRelativeActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }
    }

    private fun goToMainActivity() {
        activity?.finish()
    }
}