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
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.storeservices.StoreServices
import kotlinx.coroutines.flow.*

/**
 * Listens to changes in app configuration settings and triggers the force update UI if needed.
 */
class ForceUpdateManager(
    private val context: Context,
    private val settingsManager: ConfigurationSettingsManager
) {
    private val storeServicesClient = StoreServices()

    val shouldShowForceUpdate: Flow<Boolean> = settingsManager.settings.map {
        isAppOutdated || !arePlayServicesAvailable
    }.distinctUntilChanged().filter { it }.conflate()

    private val arePlayServicesAvailable: Boolean
    get() {
        return when (playServicesStatus) {
            StoreServicesClient.ServicesStatus.AVAILABLE -> true
            StoreServicesClient.ServicesStatus.UPDATE_REQUIRED,
            StoreServicesClient.ServicesStatus.NOT_AVAILABLE -> false
        }
    }

    val isAppOutdated: Boolean
        get() = settingsManager.isAppOutdated

    val playServicesRequireUpdate: Boolean
        get() = playServicesStatus == StoreServicesClient.ServicesStatus.UPDATE_REQUIRED

    val exposureNotificationsNotAvailable: Boolean
        get() = playServicesStatus == StoreServicesClient.ServicesStatus.NOT_AVAILABLE

    val updateRequired: Boolean
        get() = isAppOutdated || playServicesRequireUpdate || exposureNotificationsNotAvailable

    val playServicesStatus: StoreServicesClient.ServicesStatus
        get() {
            val hasExposureNotificationSettings =
                ExposureNotificationClient.hasExposureNotificationSettings(context)
            if (hasExposureNotificationSettings) {
                return StoreServicesClient.ServicesStatus.AVAILABLE
            }

            return storeServicesClient.getServicesUpdateStatus(context)
        }
}
