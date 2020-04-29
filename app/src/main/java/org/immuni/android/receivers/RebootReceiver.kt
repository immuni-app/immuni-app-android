package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.ImmuniApplication
import org.immuni.android.service.AlarmsManager
import org.immuni.android.util.log
import org.koin.core.KoinComponent

class RebootReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {

        log("Reboot event received, restarting workers...")

        // start workers

        val alarmIntent = Intent(ImmuniApplication.appContext, RestarterReceiver::class.java)
        context.sendBroadcast(alarmIntent)

        // schedule alarms

        AlarmsManager.scheduleWorks(context)
    }
}
