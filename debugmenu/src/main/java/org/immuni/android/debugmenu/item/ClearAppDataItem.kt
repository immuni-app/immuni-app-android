package org.immuni.android.debugmenu.item

import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.widget.Toast
import org.immuni.android.debugmenu.DebugMenuItem

class ClearAppDataItem : DebugMenuItem(
    "\uD83D\uDCA5 Clear app",
    { context, config ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                .clearApplicationUserData()
        } else {
            Toast.makeText(context, "This works only on Android API 19+", Toast.LENGTH_LONG)
                .show()
        }
    }
)
