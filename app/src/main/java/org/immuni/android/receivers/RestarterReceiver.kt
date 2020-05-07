package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.ImmuniApplication
import org.immuni.android.service.AlarmsManager
import org.immuni.android.service.DeleteUserDataWorker
import org.immuni.android.util.log
import org.koin.core.KoinComponent

class RestarterReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {

        log("Restarter event received, restarting workers if needed...")

        // re-schedule delete data worker

        DeleteUserDataWorker.scheduleWork(
            context
        )

        // re-schedule next alarm

        AlarmsManager.scheduleWorks(ImmuniApplication.appContext)
    }
}
