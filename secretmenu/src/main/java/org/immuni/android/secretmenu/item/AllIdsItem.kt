package org.immuni.android.secretmenu.item

import android.widget.Toast
import org.immuni.android.base.utils.DeviceUtils
import org.immuni.android.secretmenu.SecretMenuItem

class AllIdsItem: SecretMenuItem(
    "ℹ️ Copy All IDs",
    { context, config ->
        val value = config.concierge().allIds().map { "${it.name}: ${it.id}" }.joinToString()
        DeviceUtils.copyToClipBoard(context, text = value)
        Toast.makeText(context, value, Toast.LENGTH_LONG).show()
    }
)
