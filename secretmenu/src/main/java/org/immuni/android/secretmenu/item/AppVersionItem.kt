package org.immuni.android.secretmenu.item

import android.widget.Toast
import org.immuni.android.base.utils.DeviceUtils
import org.immuni.android.secretmenu.SecretMenuItem

class AppVersionItem: SecretMenuItem(
    "\uD83D\uDCF1 App Version and Build number",
    { context, config ->
        val value = "${DeviceUtils.appVersionName(context)} (${DeviceUtils.appVersionCode(context)})"
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
