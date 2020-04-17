package org.immuni.android.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.immuni.android.receivers.RestarterReceiver
import org.immuni.android.util.log

object AlarmsManager {
    // AlarmManager's events survive even if the app is not active,
    // but they are cancelled when the device reboots.

    fun scheduleWorks(appContext: Context) {
        log("Scheduling restarter work in 15 minutes...")

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        val alarmIntent = Intent(appContext, RestarterReceiver::class.java).apply {
            putExtras(Bundle().apply {
                putBoolean("alarmManager", true)
            })
        }.let { intent ->
            PendingIntent.getBroadcast(appContext, 2010020, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // cancel existing alarms

        if (alarmIntent != null && alarmManager != null) {
            alarmManager.cancel(alarmIntent)
        }

        alarmManager?.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 15 * 60 * 1000,
            alarmIntent
        )
    }
}
