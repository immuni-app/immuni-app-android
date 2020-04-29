package org.immuni.android.debugmenu.item

import org.immuni.android.debugmenu.DebugMenuItem
import org.immuni.android.debugmenu.ui.ExitActivity

class ForceQuitDataItem : DebugMenuItem(
    "❌ Quit app",
    { context, config ->
        ExitActivity.exitApplication(context)
    }
)
