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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

/**
 * Device utility methods and info.
 */
object DeviceUtils {

    private const val TAG = "DeviceUtils"

    val model
        get() = Build.MODEL

    val manufacturer: String?
        get() = Build.MANUFACTURER

    val androidVersionAPI: Int
        get() = Build.VERSION.SDK_INT

    fun appVersionName(context: Context): String {
        var version = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }

        return version
    }

    fun appVersionCode(context: Context): Long {
        var version = 0L
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = PackageInfoCompat.getLongVersionCode(pInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
        }

        return version
    }

    fun appPackage(context: Context): String {
        return context.packageName
    }

    fun copyToClipBoard(context: Context, label: String = "", text: String) {
        val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip: ClipData = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }
}
