package org.immuni.android.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.managers.SurveyManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class DeleteUserDataWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), KoinComponent {

    val surveyManager: SurveyManager by inject()
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    override suspend fun doWork(): Result {
        // Do the work here--in this case, upload the images.
        oracle.settings()?.userDataRetentionDays?.let { days ->
            surveyManager.deleteDataOlderThan(days)
        }

        Log.d("DeleteUserDataWorker", "### running DeleteUserDataWorker!")

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    companion object {
        const val WORKER_TAG = "DeleteUserDataWorker"

        fun scheduleWork(appContext: Context) {
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .build()

            val saveRequest =
                PeriodicWorkRequestBuilder<DeleteUserDataWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .addTag(WORKER_TAG)
                    .build()

            WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, saveRequest)
        }
    }
}
