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

package it.ministerodellasalute.immuni.ui.support

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.format.DateFormat
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.google.android.gms.common.GoogleApiAvailability
import it.ministerodellasalute.immuni.BuildConfig
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import kotlinx.coroutines.flow.map

class SupportViewModel(
    val context: Context,
    val settingsManager: ConfigurationSettingsManager,
    val exposureManager: ExposureManager
) : ViewModel() {

    val settings = settingsManager.settings.value

    val contactSupportEmail = liveData {
        emit(settings.supportEmail)
    }

    val contactSupportPhone = liveData {
        emit(settings.supportPhone)
    }

    val supportWorkingHours = liveData {
        emit(settings.supportPhoneOpeningTime to settings.supportPhoneClosingTime)
    }

    val osVersion = liveData {
        emit("Android API ${Build.VERSION.SDK_INT}")
    }

    val deviceModel = liveData {
        emit("${Build.MANUFACTURER} ${Build.MODEL}")
    }

    val isExposureNotificationEnabled = liveData {
        val enabled = exposureManager.isBroadcastingActive.value
        emit(
            when (enabled) {
                true -> context.getString(R.string.support_info_active_plural)
                else -> context.getString(R.string.support_info_not_active_plural)
            }
        )
    }

    val isBluetoothEnabled = liveData {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val enabled = bluetoothAdapter?.isEnabled ?: false
        emit(
            when (enabled) {
                true -> context.getString(R.string.support_info_active)
                false -> context.getString(R.string.support_info_not_active)
            }
        )
    }

    val appVersion = liveData {
        val version = String.format(
            context.getString(R.string.settings_app_version),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
        emit(version)
    }
    val googlePlayVersion = liveData {
        val version = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(
                GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                0
            )
        )
        emit(version.toString())
    }

    val connectionType = liveData {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        if (!isConnected) {
            emit(context.getString(R.string.support_info_item_connectiontype_none))
        } else {
            val isMetered = cm.isActiveNetworkMetered
            emit(
                when (isMetered) {
                    true -> context.getString(R.string.support_info_item_connectiontype_mobile)
                    false -> context.getString(R.string.support_info_item_connectiontype_wifi)
                }
            )
        }
    }

    val lastCheckDate = exposureManager.lastSuccessfulCheckDate.map { date ->
        val lastCheckDateStr = if (date != null) {
            val dateStr = DateFormat.getLongDateFormat(context).format(date)
            val timeStr = DateFormat.getTimeFormat(context).format(date)
            context.getString(R.string.support_info_item_lastencheck_date_android, dateStr, timeStr)
        } else {
            context.getString(R.string.support_info_item_lastencheck_none)
        }
        lastCheckDateStr
    }.asLiveData()
}
