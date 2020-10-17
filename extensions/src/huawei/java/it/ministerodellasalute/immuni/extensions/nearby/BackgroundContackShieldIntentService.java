package it.ministerodellasalute.immuni.extensions.nearby;

import com.huawei.hms.contactshield.ContactShield;
import com.huawei.hms.contactshield.ContactShieldCallback;
import com.huawei.hms.contactshield.ContactShieldEngine;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class BackgroundContackShieldIntentService extends IntentService {
    private static final String TAG = "ContactShield_Service";
    private ContactShieldEngine contactEngine;

    public BackgroundContackShieldIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        contactEngine = ContactShield.getContactShieldEngine(BackgroundContackShieldIntentService.this);
        Log.d(TAG, "BackgroundContackCheckingIntentService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BackgroundContackCheckingIntentService onDestroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            contactEngine.handleIntent(intent,
                    new ContactShieldCallback() {
                        @Override
                        public void onHasContact(String s) {
                            Log.d(TAG, "onHasContact");
                        }
                        @Override
                        public void onNoContact(String s) {
                            Log.d(TAG, "onNoContact");
                        }
                    });
        }
    }
}
