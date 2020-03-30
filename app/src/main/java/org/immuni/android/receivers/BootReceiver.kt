package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.immuni.android.AscoltoApplication
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.workers.BLEForegroundServiceWorker
import org.koin.core.KoinComponent
import org.koin.core.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    val btManager: BluetoothManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        btManager.scheduleBLEWorker()
    }
}
