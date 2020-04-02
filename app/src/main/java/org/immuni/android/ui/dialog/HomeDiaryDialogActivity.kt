package org.immuni.android.ui.dialog

import android.content.Intent
import android.os.Bundle
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import kotlinx.android.synthetic.main.home_diary_dialog.*
import org.immuni.android.ImmuniActivity
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.ui.log.LogActivity

class HomeDiaryDialogActivity: ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_diary_dialog)
        setDarkStatusBarFullscreen(resources.getColor(R.color.transparent))

        back.setOnClickListener {
            finish()
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
        finish()
    }
}