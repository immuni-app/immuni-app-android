package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.analytics.Pico
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.ImmuniApplication
import org.immuni.android.metrics.RebootEventReceived
import org.immuni.android.service.AlarmsManager
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject

class RebootReceiver : BroadcastReceiver(), KoinComponent {

    private val pico: Pico by inject()

    override fun onReceive(context: Context, intent: Intent) {

        log("Reboot event received, restarting workers...")

        // start workers

        val alarmIntent = Intent(ImmuniApplication.appContext, RestarterReceiver::class.java)
        context.sendBroadcast(alarmIntent)

        // schedule alarms

        AlarmsManager.scheduleWorks(context)

        GlobalScope.launch {
            pico.trackEvent(RebootEventReceived().userAction)
        }
    }
}
