package org.immuni.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import org.immuni.android.service.RequestDiagnosisKeysWorker
import org.immuni.android.service.StateUpdatedWorker

/**
 * Broadcast receiver for callbacks from exposure notification API.
 */
class ExposureNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val workManager = WorkManager.getInstance(context)
        if (ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED == action) {
            workManager.enqueueUniqueWork(
                "StateUpdatedWorker",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(StateUpdatedWorker::class.java).build()
            )
        } else if (ExposureNotificationClient.ACTION_REQUEST_DIAGNOSIS_KEYS == action) {
            workManager.enqueueUniqueWork(
                "RequestDiagnosisKeysWorker",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(RequestDiagnosisKeysWorker::class.java)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .setRequiresDeviceIdle(true)
                            .build()
                    ).build()
            )
        }
    }

    companion object {
        private const val TAG = "ENBroadcastReceiver"
    }
}