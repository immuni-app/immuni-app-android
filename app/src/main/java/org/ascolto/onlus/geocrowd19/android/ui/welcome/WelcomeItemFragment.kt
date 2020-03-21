package org.ascolto.onlus.geocrowd19.android.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.ascolto.onlus.geocrowd19.android.R
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
            0 -> R.drawable.welcome_1
            1 -> R.drawable.welcome_2
            2 -> R.drawable.welcome_3
            else -> R.drawable.welcome_4
        })

        title.text = when(position) {
            0 -> getString(R.string.welcome_1_title)
            1 -> getString(R.string.welcome_2_title)
            2 -> getString(R.string.welcome_3_title)
            else -> getString(R.string.welcome_4_title)
        }

        description.text = when(position) {
            0 -> getString(R.string.welcome_1_description)
            1 -> getString(R.string.welcome_2_description)
            2 -> getString(R.string.welcome_3_description)
            else -> getString(R.string.welcome_4_description)
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