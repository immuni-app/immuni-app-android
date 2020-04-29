package org.immuni.android.debugmenu.item

import android.widget.Toast
import org.immuni.android.debugmenu.DebugMenuItem
import org.immuni.android.debugmenu.ui.ExitActivity

class ClearUserIdentityItem : DebugMenuItem(
    "\uD83D\uDC7D Delete user ids",
    { context, config ->
        config.idsManager().resetUserIds()
        Toast.makeText(context, "User identity changed.", Toast.LENGTH_SHORT).show()
        ExitActivity.exitApplication(context)
    }
)
