package com.bendingspoons.secretmenu.item

import android.widget.Toast
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.secretmenu.SecretMenuItem

class AllIdsItem: SecretMenuItem(
    "ℹ️ Copy All IDs",
    { context, config ->
        val value = config.concierge().allIds().map { "${it.name}: ${it.id}" }.joinToString()
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
