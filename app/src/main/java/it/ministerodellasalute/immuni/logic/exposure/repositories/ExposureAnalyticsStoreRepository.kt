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

package it.ministerodellasalute.immuni.logic.exposure.repositories

import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.logic.exposure.models.AnalyticsTokenStatus
import java.util.*

class ExposureAnalyticsStoreRepository(
    private val storage: KVStorage
) {
    companion object {
        val tokenStatusKey = KVStorage.Key<AnalyticsTokenStatus>("tokenStatus")
        val infoWithExposureLastReportingMonthKey =
            KVStorage.Key<Int>("infoWithExposureLastReportingMonth")
        val infoWithoutExposureLastReportingMonthKey =
            KVStorage.Key<Int>("infoWithoutExposureLastReportingMonth")
        val dummyInfoLastReportingDateKey = KVStorage.Key<Date>("dummyInfoLastReportingDate")
    }

    var token: AnalyticsTokenStatus
        get() = storage[tokenStatusKey] ?: AnalyticsTokenStatus.None()
        set(value) = when (value) {
            is AnalyticsTokenStatus.None -> storage.delete(tokenStatusKey)
            else -> storage[tokenStatusKey] = value
        }

    var infoWithExposureLastReportingMonth: Int?
        get() = storage[infoWithExposureLastReportingMonthKey]
        set(value) {
            storage[infoWithExposureLastReportingMonthKey] = value!!
        }
    var infoWithoutExposureLastReportingMonth: Int?
        get() = storage[infoWithoutExposureLastReportingMonthKey]
        set(value) {
            storage[infoWithoutExposureLastReportingMonthKey] = value!!
        }

    var dummyInfoLastReportingDate: Date?
    get() = storage[dummyInfoLastReportingDateKey]
    set(value) {
        storage[dummyInfoLastReportingDateKey] = value!!
    }
}
