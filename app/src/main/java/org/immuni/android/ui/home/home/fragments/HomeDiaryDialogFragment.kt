package org.immuni.android.ui.home.home.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import kotlinx.android.synthetic.main.home_diary_dialog.*
import org.immuni.android.ImmuniActivity
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.dialog.FullScreenDialogDarkFragment
import org.immuni.android.ui.dialog.FullScreenDialogLightFragment
import org.immuni.android.ui.log.LogActivity

class HomeDiaryDialogFragment: FullScreenDialogDarkFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_diary_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        button.setOnClickListener {
            navigateToSurvey()
        }
    }

    private fun navigateToSurvey() {
        val intent = Intent(ImmuniApplication.appContext, LogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        findNavController().popBackStack()
    }
}