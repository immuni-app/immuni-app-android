package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.content.Intent
import android.os.Bundle
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.AddRelativeActivity

class FamilyDialogActivity: AscoltoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.family_member_add_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.transparent))

        back.setOnClickListener {
            finish()
        }

        goAhead.setOnClickListener {
            finish()
        }

        addMember.setOnClickListener {
            navigateToAddRelative()
            finish()
        }
    }

    private fun navigateToAddRelative() {
        val intent = Intent(AscoltoApplication.appContext, AddRelativeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
}