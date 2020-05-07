package org.immuni.android.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This is the [FirebaseMessagingService] listening to new FCM push notifications and tokens.
 */
internal class ImmuniFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        GlobalScope.launch(Dispatchers.Main) {
            FirebaseFCM.config.onNewPushNotification(remoteMessage)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        GlobalScope.launch {
            FirebaseFCM.tokenChannel.send(token)
        }
    }

    companion object {
        private const val TAG = "BSFCMService"
    }
}