package org.immuni.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject

class RebootReceiver : BroadcastReceiver(), KoinComponent {

    val btManager: BluetoothManager by inject()

    override fun onReceive(context: Context, intent: Intent) {

        log("Reboot event received, restarting workers...")

        // start workers

        val alarmIntent = Intent(ImmuniApplication.appContext, RestarterReceiver::class.java)
        context.sendBroadcast(alarmIntent)

        // schedule alarms

        AlarmsManager.scheduleWorks(context)
    }
}
