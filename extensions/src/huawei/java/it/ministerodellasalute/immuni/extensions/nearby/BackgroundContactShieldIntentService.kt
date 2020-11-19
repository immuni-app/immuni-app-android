package it.ministerodellasalute.immuni.extensions.nearby

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.util.Log
import com.huawei.hms.contactshield.ContactShield
import com.huawei.hms.contactshield.ContactShieldCallback
import com.huawei.hms.contactshield.ContactShieldEngine

class BackgroundContactShieldIntentService :
    IntentService(TAG) {
    private lateinit var contactEngine: ContactShieldEngine

    override fun onCreate() {
        super.onCreate()
        contactEngine =
            ContactShield.getContactShieldEngine(this@BackgroundContactShieldIntentService)
        Log.d(TAG, "BackgroundContackCheckingIntentService onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_ID = (System.currentTimeMillis() % 10000).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(NOTIFICATION_ID, Notification.Builder(this, null).build())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BackgroundContackCheckingIntentService onDestroy")
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "BackgroundContackCheckingIntentService onHandleIntent")
        Log.d(TAG, "BackgroundContackCheckingIntentService onHandleIntent: intent is null? " + (intent == null).toString())
        contactEngine.handleIntent(intent,
            object : ContactShieldCallback {
                override fun onHasContact(token: String) {
                    Log.d(TAG, "onHasContact: $token")
                    this@BackgroundContactShieldIntentService.sendBroadcast(
                        Intent(NearbyConstants.ACTION_EXPOSURE_STATE_UPDATED).apply {
                            putExtra(NearbyConstants.EXTRA_TOKEN, token)
                        }
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
