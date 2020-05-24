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

package it.ministerodellasalute.immuni.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.utils.log
import it.ministerodellasalute.immuni.workers.StateUpdatedWorker
import it.ministerodellasalute.immuni.workers.StateUpdatedWorker.Companion.TOKEN_KEY

/**
 * Broadcast receiver for callbacks from exposure notification API.
 */
class ExposureNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        log("ExposureNotificationReceiver action received")
        val action = intent.action
        val workManager = WorkManager.getInstance(context)
        if (ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED == action) {
            val token = intent.getStringExtra(ExposureNotificationClient.EXTRA_TOKEN)!!
            workManager.enqueueUniqueWork(
                "StateUpdatedWorker",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(StateUpdatedWorker::class.java)
                    .setInputData(
                        Data.Builder()
                            .putString(TOKEN_KEY, token)
                            .build()
                    )
                    .build()
            )
        }
    }
}
