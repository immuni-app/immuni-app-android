package org.immuni.android.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import org.immuni.android.util.log
import java.lang.Math.random
import kotlin.random.Random

object AlarmsManager {
    // AlarmManager's events survive even if the app is not active,
    // but they are cancelled when the device reboots.

    fun scheduleWorks(appContext: Context) {
        log("Scheduling restarter work in 15 minutes...")

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        val alarmIntent = Intent(appContext, RestarterReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(appContext, 2010020, intent, 0)
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
