package it.ministerodellasalute.immuni

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.huawei.hms.contactshield.ContactShield
import com.huawei.hms.contactshield.ContactShieldCallback
import com.huawei.hms.contactshield.ContactShieldEngine
import it.ministerodellasalute.immuni.workers.StateUpdatedWorker

class BackgroundContactShieldIntentService :
    IntentService(TAG) {
    private lateinit var contactEngine: ContactShieldEngine
    private lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()

        contactEngine =
            ContactShield.getContactShieldEngine(this@BackgroundContactShieldIntentService)
        workManager = WorkManager.getInstance(this@BackgroundContactShieldIntentService)

        Log.d(TAG, "BackgroundContackCheckingIntentService onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = (System.currentTimeMillis() % 10000).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(id, Notification.Builder(this, null).build())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BackgroundContackCheckingIntentService onDestroy")
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "BackgroundContackCheckingIntentService onHandleIntent")
        Log.d(
            TAG,
            "BackgroundContackCheckingIntentService onHandleIntent: intent is null? " + (intent == null).toString()
        )
        contactEngine.handleIntent(intent,
            object : ContactShieldCallback {
                override fun onHasContact(token: String) {
                    Log.d(TAG, "onHasContact: $token")
                    workManager.enqueueUniqueWork(
                        "StateUpdatedWorker",
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequest.Builder(StateUpdatedWorker::class.java)
                            .setInputData(
                                Data.Builder()
                                    .putString(StateUpdatedWorker.TOKEN_KEY, token)
                                    .build()
                            )
                            .build()
                    )
                }

                override fun onNoContact(s: String) {
                    Log.d(TAG, "onNoContact: $s")
                }
            })
    }

    companion object {
        private const val TAG = "ContactShield_Service"
    }
}
