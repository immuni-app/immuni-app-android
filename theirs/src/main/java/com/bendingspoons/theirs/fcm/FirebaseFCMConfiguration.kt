package com.bendingspoons.theirs.fcm

import com.bendingspoons.concierge.ConciergeManager
import com.google.firebase.messaging.RemoteMessage

interface FirebaseFCMConfiguration {
    val concierge: ConciergeManager
    suspend fun onNewPushNotification(message: RemoteMessage)
    suspend fun onNewToken(token: String)
}
