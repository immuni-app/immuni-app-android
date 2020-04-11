package org.immuni.android.service

import android.content.Context
import androidx.work.*
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyManager
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit

class DeleteUserDataWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), KoinComponent {

    val database: ImmuniDatabase by inject()
    val surveyManager: SurveyManager by inject()
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    override suspend fun doWork(): Result = coroutineScope {

        val deleteSettings = async {
            oracle.settings()?.userDataRetentionDays?.let { days ->
                surveyManager.deleteDataOlderThan(days)
            }
        }

        val deleteDatabase = async {
            oracle.settings()?.userDataRetentionDays?.let { days ->
                database.bleContactDao().removeOlderThan(
                    timestamp = Calendar.getInstance().apply {
                        add(Calendar.DATE, -days)
                    }.timeInMillis
                )
            }
        }

        deleteSettings.await()
        deleteDatabase.await()

        log("running DeleteUserDataWorker!")

        // Indicate whether the task finished successfully with the Result
        Result.success()
    }

    companion object {
        const val WORKER_TAG = "DeleteUserDataWorker"

        fun scheduleWork(appContext: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .build()

            val saveRequest =
                PeriodicWorkRequestBuilder<DeleteUserDataWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .setInitialDelay(1, TimeUnit.HOURS)
                    .addTag(WORKER_TAG)
                    .build()

            WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, saveRequest)
        }
    }
}
