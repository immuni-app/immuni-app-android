package org.immuni.android.config

import org.immuni.android.api.ImmuniAPI
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.networking.Networking
import org.immuni.android.fcm.FirebaseFCMConfiguration
import com.google.firebase.messaging.RemoteMessage
import org.immuni.android.networking.api.model.DevicesRequest
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniFirebaseFCMConfiguration: FirebaseFCMConfiguration, KoinComponent {

    override suspend fun onNewPushNotification(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        /*if(remoteMessage.data["immuni_key"] == "immuni_value") {
            toast(ImmuniApplication.appContext,"C'è un messaggio importante per te!")
        }*/

        // Check if message contains a notification payload.
        //remoteMessage.notification?.let {
        //    toast("NOTIFICATION ${it.body}")
        //}
    }

    override suspend fun onNewToken(token: String) {
        val networking: Networking<ImmuniSettings> by inject()

        // be sure to call settings before
        networking.api.settings()

        networking.customServiceAPI(ImmuniAPI::class).fcmNotificationToken(FcmTokenRequest(
            token = token
        ))
    }
}
