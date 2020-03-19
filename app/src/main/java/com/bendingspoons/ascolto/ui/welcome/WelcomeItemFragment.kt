package com.bendingspoons.ascolto.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bendingspoons.ascolto.R
import kotlinx.android.synthetic.main.welcome_item_fragment.*

class WelcomeItemFragment : Fragment() {

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        position = arguments?.getInt("position") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_item_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image.setImageResource(when(position) {
            0 -> R.drawable.ic_back_form
            1 -> R.drawable.ic_cross
            2 -> R.drawable.ic_tab_home
            else -> R.drawable.ic_cross
        })

        title.text = when(position) {
            0 -> "$position"
            1 -> "$position"
            2 -> "$position"
            else -> "$position"
        }

        description.text = when(position) {
            0 -> "$position"
            1 -> "$position"
            2 -> "$position"
            else -> "$position"
        }
    }

    private fun goToMainActivity() {
        /*
        val intent = Intent(AscoltoApplication.appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity?.startActivity(intent)
        activity?.finish()

         */
    }

}