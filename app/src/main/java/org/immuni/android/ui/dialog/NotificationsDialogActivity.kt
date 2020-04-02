package org.immuni.android.ui.dialog

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.bendingspoons.base.extensions.setLightStatusBarFullscreen
import kotlinx.android.synthetic.main.enable_notifications_dialog.*
import kotlinx.android.synthetic.main.family_member_add_dialog.back
import org.immuni.android.ImmuniActivity
import org.immuni.android.ImmuniApplication
import org.immuni.android.R

class NotificationsDialogActivity: ImmuniActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enable_notifications_dialog)
        setLightStatusBarFullscreen(resources.getColor(R.color.transparent))

        button.setOnClickListener {
            openNotificationSettings()
            finish()
        }

        back.setOnClickListener {
            finish()
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
        startActivity(intent)
    }

    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, ImmuniApplication.appContext.packageName)
            startActivity(intent)
        } else {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
            startActivity(intent)
        }
    }
}