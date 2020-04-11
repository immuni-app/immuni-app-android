package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.util.log
import org.immuni.android.workers.RestarterWorker
import org.immuni.android.workers.DeleteUserDataWorker
import org.koin.core.KoinComponent
import org.koin.core.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    val btManager: BluetoothManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        log("Boot event received, restarting workers...")
        btManager.scheduleBLEWorker(context)
        DeleteUserDataWorker.scheduleWork(context)
        RestarterWorker.scheduleWork(context)
    }
}
