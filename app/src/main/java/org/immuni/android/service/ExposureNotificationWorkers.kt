package org.immuni.android.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.immuni.android.managers.ExposureNotificationManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class StateUpdatedWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    private val exposureNotificationManager: ExposureNotificationManager by inject()

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
        // 1) check if the user has to be notified
    }
}

class RequestDiagnosisKeysWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    private val exposureNotificationManager: ExposureNotificationManager by inject()

    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
        // 1) fetch new keys from the server and put them inside an array
        // 2) add the keys inside the array by paginating them if needed
    }
}
