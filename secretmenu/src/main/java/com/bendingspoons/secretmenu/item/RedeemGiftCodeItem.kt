package com.bendingspoons.secretmenu.item

import android.content.Intent
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.secretmenu.ui.redeemgiftcode.RedeemGiftCodeActivity

class RedeemGiftCodeItem: SecretMenuItem(
    "\uD83C\uDF81 Redeem Gift Code",
    { context, config ->
        val intent = Intent(context, RedeemGiftCodeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
)
