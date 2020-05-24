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

package it.ministerodellasalute.immuni.ui.forceupdate

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.GoogleApiAvailability
import it.ministerodellasalute.immuni.extensions.playstore.PlayStoreActions
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import org.koin.core.KoinComponent

class ForceUpdateViewModel(
    private val forceUpdateManager: ForceUpdateManager
) : ViewModel(), KoinComponent {
    fun goToPlayStoreAppDetails(context: Context) {
        PlayStoreActions.goToPlayStoreAppDetails(context)
    }

    val updateRequired: Boolean
        get() = forceUpdateManager.updateRequired

    val isAppOutdated: Boolean
        get() = forceUpdateManager.isAppOutdated

    val playServicesRequireUpdate: Boolean
        get() = forceUpdateManager.playServicesRequireUpdate

    val exposureNotificationsNotAvailable: Boolean
        get() = forceUpdateManager.exposureNotificationsNotAvailable

    fun updatePlayServices(context: Context) {
        val playServicesPackage = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE
        PlayStoreActions.goToPlayStoreAppDetails(context, playServicesPackage)
    }
}
