package org.immuni.android.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.ui.onboarding.Onboarding
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class CheckAppStateWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), KoinComponent {

    val onboarding: Onboarding by inject()
    val database: ImmuniDatabase by inject()
    val surveyManager: SurveyManager by inject()
    val bluetoothManager: BluetoothManager by inject()
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    override suspend fun doWork(): Result = coroutineScope {
        Log.d("CheckAppStateWorker", "### running CheckAppStatePeriodicWorker!")
        // if the user didn't do the onboarding yet, not run the worker
        if(!onboarding.isComplete()) {
            Result.success()
        }
        else {

            // TODO also check for ble/localization/notification/battery optimisation
            // and show a notification

            bluetoothManager.scheduleBLEWorker(applicationContext)
            Result.success()
        }
    }

    companion object {
        const val WORKER_TAG = "CheckAppStateWorker"

        fun scheduleWork(appContext: Context) {
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .build()

            val saveRequest =
                PeriodicWorkRequestBuilder<CheckAppStateWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .addTag(WORKER_TAG)
                    .build()

            WorkManager.getInstance(appContext)
                .enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, saveRequest)
        }
    }
}
