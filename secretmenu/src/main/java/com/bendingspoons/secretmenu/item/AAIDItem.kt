package com.bendingspoons.secretmenu.item

import android.widget.Toast
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.secretmenu.SecretMenuItem

class AAIDItem: SecretMenuItem(
    "ℹ️ Copy AAID",
    { context, config ->
        val value = config.concierge().aaid?.id ?: ""
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
