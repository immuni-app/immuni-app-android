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

package it.ministerodellasalute.immuni.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import it.ministerodellasalute.immuni.logic.exposure.ExposureAnalyticsManager
import java.util.*
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.KoinComponent
import org.koin.core.inject

class ExposureAnalyticsWorker(
    appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    companion object {
        const val SERVER_DATE_INPUT_DATA_KEY = "serverDate"
    }

    private val analyticsManager: ExposureAnalyticsManager by inject()

    override suspend fun doWork(): Result {
        withTimeoutOrNull(9 * 60 * 1000) {
            val serverDate = params.inputData.getLong(SERVER_DATE_INPUT_DATA_KEY, 0)
            analyticsManager.onRequestDiagnosisKeysSucceeded(Date(serverDate))
        }
        return Result.success()
    }
}
