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

package it.ministerodellasalute.immuni.logic.forceupdate

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import kotlinx.coroutines.flow.*

/**
 * Listens to changes in app configuration settings and triggers the force update UI if needed.
 */
class ForceUpdateManager(
    private val context: Context,
    private val settingsManager: ConfigurationSettingsManager
) {
    val shouldShowForceUpdate: Flow<Boolean> = settingsManager.settings.map {
        isAppOutdated || !arePlayServicesAvailable
    }.distinctUntilChanged().filter { it }.conflate()

    private val arePlayServicesAvailable: Boolean
    get() {
        return when (playServicesStatus) {
            PlayServicesStatus.AVAILABLE -> true
            PlayServicesStatus.UPDATE_REQUIRED,
            PlayServicesStatus.NOT_AVAILABLE -> false
        }
    }

    val isAppOutdated: Boolean
        get() = settingsManager.isAppOutdated

    val playServicesRequireUpdate: Boolean
        get() = playServicesStatus == PlayServicesStatus.UPDATE_REQUIRED

    val exposureNotificationsNotAvailable: Boolean
        get() = playServicesStatus == PlayServicesStatus.NOT_AVAILABLE

    val updateRequired: Boolean
        get() = isAppOutdated || playServicesRequireUpdate || exposureNotificationsNotAvailable

    val playServicesStatus: PlayServicesStatus
        get() {
            val hasExposureNotificationSettings =
                ExposureNotificationClient.hasExposureNotificationSettings(context)
            if (hasExposureNotificationSettings) {
                return PlayServicesStatus.AVAILABLE
            }

            val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            val isUpdateRequired = status in listOf(
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
                ConnectionResult.SERVICE_UPDATING
            )
            return if (isUpdateRequired) PlayServicesStatus.UPDATE_REQUIRED else PlayServicesStatus.NOT_AVAILABLE
        }
}

enum class PlayServicesStatus {
    NOT_AVAILABLE,
    UPDATE_REQUIRED,
    AVAILABLE
}
