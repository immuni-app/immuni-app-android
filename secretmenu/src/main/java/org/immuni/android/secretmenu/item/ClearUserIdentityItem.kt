package org.immuni.android.secretmenu.item

import android.widget.Toast
import org.immuni.android.secretmenu.SecretMenuItem
import org.immuni.android.secretmenu.ui.ExitActivity

class ClearUserIdentityItem : SecretMenuItem(
    "\uD83D\uDC7D Delete user ids",
    { context, config ->
        config.concierge().resetUserIds()
        Toast.makeText(context, "User identity changed.", Toast.LENGTH_SHORT).show()
        ExitActivity.exitApplication(context)
    }
)
