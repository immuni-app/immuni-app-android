package org.immuni.android.secretmenu.item

import android.widget.Toast
import org.immuni.android.base.utils.DeviceUtils
import org.immuni.android.secretmenu.SecretMenuItem

class DeviceInfoItem: SecretMenuItem(
    "\uD83D\uDCF1 Device/OS Info",
    { context, config ->
        val value = "${DeviceUtils.manufacturer} ${DeviceUtils.model} Android API ${DeviceUtils.androidVersionAPI}"
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
