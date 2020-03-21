package com.bendingspoons.ascolto.ui.dialog

import android.os.Bundle
import com.bendingspoons.ascolto.AscoltoActivity
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.toast
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.*

class AddFamilyMemberActivity: AscoltoActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBarFullscreen(resources.getColor(android.R.color.transparent))
        setContentView(R.layout.family_member_add_dialog)

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