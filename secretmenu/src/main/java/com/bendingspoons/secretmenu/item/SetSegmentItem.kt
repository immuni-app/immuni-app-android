package com.bendingspoons.secretmenu.item

import android.content.Intent
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.secretmenu.ui.redeemgiftcode.RedeemGiftCodeActivity
import com.bendingspoons.secretmenu.ui.setsegment.SetSegmentActivity

class SetSegmentItem: SecretMenuItem(
    "\uD83D\uDD2E Set Experiment Segment",
    { context, config ->
        val intent = Intent(context, SetSegmentActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
)
