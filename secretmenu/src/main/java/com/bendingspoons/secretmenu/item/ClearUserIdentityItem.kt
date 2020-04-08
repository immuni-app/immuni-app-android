package com.bendingspoons.secretmenu.item

import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.widget.Toast
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.secretmenu.ui.ExitActivity
import kotlin.system.exitProcess

class ClearUserIdentityItem : SecretMenuItem(
    "\uD83D\uDC7D Delete user ids",
    { context, config ->
        config.concierge().resetUserIds()
        Toast.makeText(context, "User identity changed.", Toast.LENGTH_SHORT).show()
        ExitActivity.exitApplication(context)
    }
)
