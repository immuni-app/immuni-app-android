package com.bendingspoons.secretmenu.item

import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.secretmenu.ui.ExitActivity
import kotlin.system.exitProcess

class ForceQuitDataItem : SecretMenuItem(
    "❌ Quit app",
    { context, config ->
        ExitActivity.exitApplication(context)
    }
)
