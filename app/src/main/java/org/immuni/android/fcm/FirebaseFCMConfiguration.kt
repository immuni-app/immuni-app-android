package org.immuni.android.fcm

import com.google.firebase.messaging.RemoteMessage

/**
 * This is the FCM configuration
 * the app injects into [TheirsConfiguration].
 */
interface FirebaseFCMConfiguration {

    /**
     * The app here has the change to show a custom notification or do an action.
     */
    suspend fun onNewPushNotification(message: RemoteMessage)

    /**
     * The app here can read the FCM token and send it to the backend.
     */
    suspend fun onNewToken(token: String)
}
