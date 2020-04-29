package org.immuni.android.config

import org.immuni.android.api.ImmuniAPI
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.api.model.FcmTokenRequest
import org.immuni.android.ids.IdsManager
import org.immuni.android.networking.Networking
import org.immuni.android.fcm.FirebaseFCMConfiguration
import com.google.firebase.messaging.RemoteMessage
import org.immuni.android.networking.api.model.DevicesRequest
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniFirebaseFCMConfiguration: FirebaseFCMConfiguration, KoinComponent {
    override val ids: IdsManager by inject()

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
        val networking: Networking<ImmuniSettings, ImmuniMe> by inject()

        // be sure to call devices before
        networking.api.devices(
            DevicesRequest(
                uniqueId = ids.backupPersistentId.id
            )
        )

        networking.customServiceAPI(ImmuniAPI::class).fcmNotificationToken(FcmTokenRequest(
            token = token
        ))
    }
}
