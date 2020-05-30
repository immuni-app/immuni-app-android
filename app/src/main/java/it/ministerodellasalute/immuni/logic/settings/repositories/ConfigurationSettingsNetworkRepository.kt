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

package it.ministerodellasalute.immuni.logic.settings.repositories

import it.ministerodellasalute.immuni.api.immuniApiCall
import it.ministerodellasalute.immuni.api.services.ConfigurationSettingsService
import it.ministerodellasalute.immuni.logic.settings.models.FetchFaqsResult
import it.ministerodellasalute.immuni.logic.settings.models.FetchSettingsResult
import it.ministerodellasalute.immuni.network.api.NetworkResource

class ConfigurationSettingsNetworkRepository(
    private val configurationSettingsService: ConfigurationSettingsService
) {
    suspend fun fetchSettings(buildVersion: Long): FetchSettingsResult {
        val resource = immuniApiCall {
            configurationSettingsService.settings(build = buildVersion)
        }

        return when (resource) {
            is NetworkResource.Success -> FetchSettingsResult.Success(
                resource.data!!,
                resource.serverDate!!
            )
            is NetworkResource.Error -> {
                return if (resource.error == null) FetchSettingsResult.ConnectionError
                else FetchSettingsResult.ServerError
            }
        }
    }

    suspend fun fetchFaqs(url: String): FetchFaqsResult {
        val resource = immuniApiCall {
            configurationSettingsService.faqs(url)
        }

        return when (resource) {
            is NetworkResource.Success -> FetchFaqsResult.Success(resource.data!!.faqs)
            is NetworkResource.Error -> {
                return if (resource.error == null) FetchFaqsResult.ConnectionError
                else FetchFaqsResult.ServerError
            }
        }
    }
}
