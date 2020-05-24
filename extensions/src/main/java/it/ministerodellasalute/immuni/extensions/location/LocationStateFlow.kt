/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion

/**
 * LocationStateFlow.
 *
 * Use this flow to react to location state changes.
 */
class LocationStateFlow private constructor(
    private val context: Context,
    private val flow: MutableStateFlow<Boolean>
) : StateFlow<Boolean> by flow {
    constructor(context: Context) : this(context, MutableStateFlow(false))

    fun isLocationEnabled(): Boolean {
        return locationManager?.let { LocationManagerCompat.isLocationEnabled(it) } ?: false
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                flow.value = isLocationEnabled()
            }
        }
    }

    private val locationManager: LocationManager? by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    init {
        flow.value = isLocationEnabled()
        // Register for broadcasts on when the set of enabled location providers changes
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(mReceiver, filter)
        onCompletion {
            context.unregisterReceiver(mReceiver)
        }
    }
}
