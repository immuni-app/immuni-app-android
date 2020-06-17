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

package it.ministerodellasalute.immuni.extensions.utils

import android.app.ActivityManager
import android.content.Context

/**
 * Get a [ActivityManager.MemoryInfo] object for the device's current memory status.
 */
fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return ActivityManager.MemoryInfo().also { memoryInfo ->
        activityManager.getMemoryInfo(memoryInfo)
    }
}

/**
 * Return the total RAM of the device in MB.
 */
fun totalRamMB(context: Context): Long {
    return getMemoryInfo(context).totalMem / (1024L * 1024L)
}

/**
 * Return is this device is an high end device with enough RAM.
 */
fun isHighEndDevice(context: Context): Boolean {
    return totalRamMB(context) > 2000
}

/**
 * Return is this device is a top level device with a lot of RAM.
 */
fun isTopEndDevice(context: Context): Boolean {
    return totalRamMB(context) > 4500
}
