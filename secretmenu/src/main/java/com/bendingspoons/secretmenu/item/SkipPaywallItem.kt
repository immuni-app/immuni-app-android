package com.bendingspoons.secretmenu.item

import android.widget.Toast
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.secretmenu.SecretMenuItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SkipPaywallItem: SecretMenuItem(
    "\uD83D\uDCB8 Subscribe/Unsubscribe",
    { context, config ->
        GlobalScope.launch {
            SecretMenu.instance.oracle.me()?.let {
                SecretMenu.instance.oracle.forceIsSubscribed(!it.isSubscribed)
            }

        }
    }
)
