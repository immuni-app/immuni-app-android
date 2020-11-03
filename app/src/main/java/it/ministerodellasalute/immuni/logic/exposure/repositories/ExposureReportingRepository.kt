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

import com.squareup.moshi.JsonClass
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager.Companion.DAYS_OF_SELF_ISOLATION
import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import java.util.*
import kotlinx.coroutines.flow.StateFlow

class ExposureReportingRepository(
    private val storage: KVStorage
) {
    @JsonClass(generateAdapter = true)
    data class ExposureSummaryList(val values: List<ExposureSummary>)

    @JsonClass(generateAdapter = true)
    data class CountryOfInterestList(val values: List<CountryOfInterest>)

    companion object {
        private val summariesKey = KVStorage.Key<ExposureSummaryList>("summaries")
        private val lastProcessedChunkKey = KVStorage.Key<Int>("LastProcessedChunk")
        private val lastSuccessfulCheckDateKey = KVStorage.Key<Date>("LastSuccessfulCheckDate")
        private val countriesOfInterestKey = KVStorage.Key<CountryOfInterestList>("CountriesOfInterest")
    }

    fun getSummaries(): List<ExposureSummary> {
        return storage[summariesKey]?.values ?: listOf()
    }

    fun addSummary(summary: ExposureSummary) {
        synchronized(this) {
            val oldSummaries = getSummaries()
            storage[summariesKey] = ExposureSummaryList(oldSummaries + summary)
        }
    }

    fun resetSummaries() {
        storage.delete(summariesKey)
    }

    // cleanup entities that are older than DAYS_OF_SELF_ISOLATION
    fun deleteOldSummaries(serverDate: Date) {
        val referenceDate = Calendar.getInstance().apply {
            timeInMillis = serverDate.time
            add(Calendar.DAY_OF_YEAR, -DAYS_OF_SELF_ISOLATION)
        }.time

        synchronized(this) {
            val oldSummaries = getSummaries()
            val newSummaries = oldSummaries.filter { it.lastExposureDate >= referenceDate }
            if (newSummaries.isEmpty()) {
                storage.delete(summariesKey)
            } else {
                storage[summariesKey] = ExposureSummaryList(newSummaries)
            }
        }
    }

    fun lastProcessedChunk(default: Int): Int {
        return storage[lastProcessedChunkKey, default]
    }

    fun setLastProcessedChunk(value: Int?) {
        if (value == null) {
            storage.delete(lastProcessedChunkKey)
        } else {
            storage[lastProcessedChunkKey] = value
        }
    }

    val lastSuccessfulCheckDate: StateFlow<Date?>
        get() = storage.stateFlow(lastSuccessfulCheckDateKey)

    fun setLastSuccessfulCheckDate(value: Date) {
        storage[lastSuccessfulCheckDateKey] = value
    }

    fun getCountriesOfInterest(): List<CountryOfInterest> {
        return storage[countriesOfInterestKey]?.values ?: listOf()
    }

    fun setCountriesOfInterest(countries: List<CountryOfInterest>) {
        storage[countriesOfInterestKey] = CountryOfInterestList(countries)
    }
}
