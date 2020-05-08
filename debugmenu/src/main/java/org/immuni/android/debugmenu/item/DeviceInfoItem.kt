package org.immuni.android.debugmenu.item

import android.widget.Toast
import org.immuni.android.debugmenu.DebugMenuItem
import org.immuni.android.extensions.utils.DeviceUtils

class DeviceInfoItem : DebugMenuItem(
    "\uD83D\uDCF1 Device/OS Info",
    { context, config ->
        val value = "${DeviceUtils.manufacturer} ${DeviceUtils.model} Android API ${DeviceUtils.androidVersionAPI}"
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
