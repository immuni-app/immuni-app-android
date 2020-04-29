package org.immuni.android.secretmenu.item

import org.immuni.android.secretmenu.SecretMenuItem
import org.immuni.android.secretmenu.ui.ExitActivity

class ForceQuitDataItem : SecretMenuItem(
    "❌ Quit app",
    { context, config ->
        ExitActivity.exitApplication(context)
    }
)
