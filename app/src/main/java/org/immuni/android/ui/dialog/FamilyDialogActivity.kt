package org.immuni.android.ui.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import org.immuni.android.ImmuniActivity
import org.immuni.android.R


class FamilyDialogActivity: ImmuniActivity() {

    companion object {
        const val REQUEST_CODE_FAMILY_DIALOG = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.family_member_add_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.transparent))

        back.setOnClickListener {
            finish()
        }

        goAhead.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }

        addMember.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    /*
    private fun navigateToAddRelative() {
        val intent = Intent(AscoltoApplication.appContext, AddRelativeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }*/
}