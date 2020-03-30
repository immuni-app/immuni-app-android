package org.immuni.android.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.bendingspoons.base.extensions.setDarkStatusBarFullscreen
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.enable_geolocation_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import org.immuni.android.AscoltoActivity
import org.immuni.android.AscoltoApplication
import org.immuni.android.R
import org.immuni.android.toast

class GeolocationDialogActivity: AscoltoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enable_geolocation_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.transparent))

        button.setOnClickListener {
            openAppSettings()

            //val geolocationManager: GeolocationManager by inject()
            //geolocationManager.requestPermissions(activity = activity as AppCompatActivity)

        }

        back.setOnClickListener {
            finish()
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + AscoltoApplication.appContext.packageName)
        startActivity(intent)

        finish()
    }
}