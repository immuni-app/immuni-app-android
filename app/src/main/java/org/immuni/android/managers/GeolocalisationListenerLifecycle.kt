package org.immuni.android.managers

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.koin.core.KoinComponent
import org.koin.core.inject

class GeolocalisationListenerLifecycle(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val callback: (Boolean) -> Unit
): LifecycleObserver {

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                callback(GeolocationManager.globalLocalisationEnabled(context))
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(mReceiver, filter)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stop() {
        context.unregisterReceiver(mReceiver)
    }
}

