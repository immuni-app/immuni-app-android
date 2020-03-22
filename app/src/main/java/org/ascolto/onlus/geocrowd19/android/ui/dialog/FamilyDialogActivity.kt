package org.ascolto.onlus.geocrowd19.android.ui.dialog

import android.os.Bundle
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import org.ascolto.onlus.geocrowd19.android.AscoltoActivity
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.toast

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
            toast("TODO go to add family member")
        }
    }
}