package org.immuni.android.extensions.utils

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