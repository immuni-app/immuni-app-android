package it.ministerodellasalute.immuni.extensions.nearby

import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.huawei.hms.contactshield.ContactShield
import com.huawei.hms.contactshield.ContactShieldCallback
import com.huawei.hms.contactshield.ContactShieldEngine

class BackgroundContactShieldIntentService :
    JobIntentService() {
    private lateinit var contactEngine: ContactShieldEngine

    override fun onCreate() {
        super.onCreate()
        contactEngine =
            ContactShield.getContactShieldEngine(this@BackgroundContactShieldIntentService)
        Log.d(TAG, "BackgroundContackCheckingIntentService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BackgroundContackCheckingIntentService onDestroy")
    }

    override fun onHandleWork(intent: Intent) {
        contactEngine.handleIntent(intent,
            object : ContactShieldCallback {
                override fun onHasContact(token: String) {
                    Log.d(TAG, "onHasContact")
                    this@BackgroundContactShieldIntentService.sendBroadcast(
                        Intent(NearbyConstants.ACTION_EXPOSURE_STATE_UPDATED).apply {
                            putExtra(NearbyConstants.EXTRA_TOKEN, token)
                        }
                    )
                }

                override fun onNoContact(s: String) {
                    Log.d(TAG, "onNoContact")
                }
            })
    }

    companion object {
        private const val TAG = "ContactShield_Service"
    }
}
