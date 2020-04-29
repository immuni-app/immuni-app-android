package org.immuni.android.config

import org.immuni.android.networking.CustomOracleAPI
import org.immuni.android.networking.model.ImmuniMe
import org.immuni.android.networking.model.ImmuniSettings
import org.immuni.android.networking.model.FcmTokenRequest
import org.immuni.android.ids.ConciergeManager
import org.immuni.android.networking.Oracle
import org.immuni.android.networking.api.model.DevicesRequest
import org.immuni.android.fcm.FirebaseFCMConfiguration
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniFirebaseFCMConfiguration: FirebaseFCMConfiguration, KoinComponent {
    override val concierge: ConciergeManager by inject()

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
        val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

        // be sure to call devices before
        oracle.api.devices(DevicesRequest(
            uniqueId = concierge.backupPersistentId.id
        ))

        oracle.customServiceAPI(CustomOracleAPI::class).fcmNotificationToken(FcmTokenRequest(
            token = token
        ))
    }
}
