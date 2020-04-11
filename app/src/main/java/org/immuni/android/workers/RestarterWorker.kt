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
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

class RestarterWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), KoinComponent {

    val onboarding: Onboarding by inject()
    val bluetoothManager: BluetoothManager by inject()

    override suspend fun doWork(): Result = coroutineScope {
        log("Running restarter work...")

        // if the user didn't do the onboarding yet, not run the worker
        if(!onboarding.isComplete()) {
            scheduleWork(applicationContext)
            Result.success()
        }
        else {
            scheduleWork(applicationContext)
            bluetoothManager.scheduleBLEWorker(applicationContext)
            Result.success()
        }
    }

    companion object {
        const val WORKER_TAG = "RestarterWorker"

        fun scheduleWork(appContext: Context) {
            val DELAY = 5L
            log("Scheduling restarter work in $DELAY minutes...")
            val notificationWork = OneTimeWorkRequestBuilder<RestarterWorker>().apply {
                setInitialDelay(DELAY, TimeUnit.MINUTES).addTag(WORKER_TAG)
            }
            WorkManager.getInstance(appContext).enqueue(notificationWork.build())
        }
    }
}
