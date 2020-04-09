package com.bendingspoons.theirs.fcm

import android.util.Log
import com.bendingspoons.theirs.Theirs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class BSFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        GlobalScope.launch(Dispatchers.Main) {
            Theirs.config.firebaseFCMConfig().onNewPushNotification(remoteMessage)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        GlobalScope.launch {
            FirebaseFCM.tokenChannel.send(token)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}