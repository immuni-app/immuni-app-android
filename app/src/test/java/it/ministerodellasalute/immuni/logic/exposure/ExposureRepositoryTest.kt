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

package it.ministerodellasalute.immuni.logic.exposure

import android.content.SharedPreferences
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager.Companion.DAYS_OF_SELF_ISOLATION
import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.immuniMoshi
import it.ministerodellasalute.immuni.logic.exposure.models.CountryOfInterest
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureSummary
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.testutils.MockSharedPreferences
import java.util.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExposureRepositoryTest {
    private lateinit var repository: ExposureReportingRepository
    private val sharedPreferences: SharedPreferences = MockSharedPreferences()
    private val storage = KVStorage(
        name = "Test",
        context = null,
        cacheInMemory = false,
        encrypted = false,
        moshi = immuniMoshi,
        _sharedPrefs = sharedPreferences
    )

    @Before
    fun setup() {
        storage.clear()
        repository = ExposureReportingRepository(storage)
    }

    @Test
    fun `manager deletes data after 14 days`() {
        val summary =
            ExposureSummary(
                date = Date(),
                lastExposureDate = Date(),
                matchedKeyCount = 1,
                maximumRiskScore = 5,
                highRiskAttenuationDurationMinutes = 30,
                mediumRiskAttenuationDurationMinutes = 30,
                lowRiskAttenuationDurationMinutes = 30,
                riskScoreSum = 5,
                exposureInfos = listOf()
            )
        repository.addSummary(summary)
        assertTrue(repository.getSummaries().isNotEmpty())

        val serverDateAfterDaysOfSelfIsolationMinusOneSecond = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, DAYS_OF_SELF_ISOLATION)
            add(Calendar.SECOND, -1)
        }.time
        repository.deleteOldSummaries(serverDateAfterDaysOfSelfIsolationMinusOneSecond)
        assertTrue(repository.getSummaries().isNotEmpty())

        val serverDateAfterDaysOfSelfIsolationPlusOneSecond = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, DAYS_OF_SELF_ISOLATION)
            add(Calendar.SECOND, 1)
        }.time
        repository.deleteOldSummaries(serverDateAfterDaysOfSelfIsolationPlusOneSecond)
        assertTrue(repository.getSummaries().isEmpty())
    }

    @Test
    fun `manage list of countries of interest`() {
        val country = CountryOfInterest("DK", "DANIMARCA", Date())

        repository.setCountriesOfInterest(listOf(country))
        assertTrue(repository.getCountriesOfInterest().isNotEmpty())

        val countries = repository.getCountriesOfInterest().toMutableList()
        countries.remove(country)

        repository.setCountriesOfInterest(countries)
        assertTrue(repository.getCountriesOfInterest().isEmpty())
    }
}
