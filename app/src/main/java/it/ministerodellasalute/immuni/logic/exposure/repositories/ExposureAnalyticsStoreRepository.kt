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
import java.util.*

class ExposureAnalyticsStoreRepository(
    private val storage: KVStorage
) {
    companion object {
        val installDateKey = KVStorage.Key<Date>("installDate")
        val infoWithExposureLastReportingMonthKey =
            KVStorage.Key<Int>("infoWithExposureLastReportingMonth")
        val infoWithoutExposureReportingDateKey =
            KVStorage.Key<Date>("infoWithoutExposureReportingDate")
        val dummyInfoReportingDateKey = KVStorage.Key<Date>("dummyInfoReportingDate")
    }

    var installDate: Date?
        get() = storage[installDateKey]
        set(value) {
            storage[installDateKey] = value!!
        }

    var infoWithExposureLastReportingMonth: Int?
        get() = storage[infoWithExposureLastReportingMonthKey]
        set(value) {
            storage[infoWithExposureLastReportingMonthKey] = value!!
        }

    var infoWithoutExposureReportingDate: Date?
        get() = storage[infoWithoutExposureReportingDateKey]
        set(value) {
            storage[infoWithoutExposureReportingDateKey] = value!!
        }

    var dummyInfoReportingDate: Date?
    get() = storage[dummyInfoReportingDateKey]
    set(value) {
        storage[dummyInfoReportingDateKey] = value!!
    }
}
