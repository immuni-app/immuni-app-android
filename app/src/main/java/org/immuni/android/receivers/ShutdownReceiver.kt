package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.immuni.android.util.log
import org.koin.core.KoinComponent

class ShutdownReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {

        log("Shutdown event received...")
    }
}
