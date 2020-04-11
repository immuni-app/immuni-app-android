package org.immuni.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.util.log
import org.immuni.android.service.DeleteUserDataWorker
import org.koin.core.KoinComponent
import org.koin.core.inject

class RestarterReceiver : BroadcastReceiver(), KoinComponent {

    val btManager: BluetoothManager by inject()

    override fun onReceive(context: Context, intent: Intent) {

        log("Restarter event received, restarting workers if needed...")

        btManager.scheduleBLEWorker(context)
        DeleteUserDataWorker.scheduleWork(context)

        // re-schedule next alarm

        AlarmsManager.scheduleWorks(ImmuniApplication.appContext)
    }
}
