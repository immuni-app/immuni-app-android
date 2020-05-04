package org.immuni.android.service

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.immuni.android.api.APIManager
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit

class DeleteUserDataWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val database: ImmuniDatabase by inject()
    private val api: APIManager by inject()

    override suspend fun doWork(): Result = coroutineScope {
        log("running DeleteUserDataWorker!")

        withContext(Dispatchers.Default) {
            api.latestSettings()?.userDataRetentionDays?.let { days ->
                database.bleContactDao().removeOlderThan(
                    timestamp = Calendar.getInstance().apply {
                        add(Calendar.DATE, -days)
                    }.timeInMillis
                )
            }
        }

        // Indicate whether the task finished successfully with the Result
        Result.success()
    }

    companion object {
        private const val WORKER_TAG = "DeleteUserDataWorker"

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
