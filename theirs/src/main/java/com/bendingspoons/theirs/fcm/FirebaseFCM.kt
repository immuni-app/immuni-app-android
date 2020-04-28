package com.bendingspoons.theirs.fcm

import android.content.Context
import android.util.Log
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.concierge.ConciergeCustomIdProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * This class manages the push notification tokens.
 * It exposes a [ConflatedBroadcastChannel] containing the last token.
 * It implements [ConciergeCustomIdProvider] to be able to set the token into [Concierge].
 *
 * @param context
 * @param config the FCM configuration
 */
internal class FirebaseFCM(
    context: Context,
    config: FirebaseFCMConfiguration
) : ConciergeCustomIdProvider {

    companion object {
        val tokenChannel = ConflatedBroadcastChannel<String>()
        const val TAG = "FirebaseFCM"
    }

    private var token: String? = null

    init {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                Log.w(TAG, "### Firebase FCM token: ${token}", task.exception)
                token?.let {
                    GlobalScope.launch {
                        tokenChannel.send(it)
                        config.onNewToken(it)
                    }
                }
            })

        config.concierge.registerCustomIdProvider(this)
        GlobalScope.launch {
            tokenChannel.consumeEach {
                token = it
            }
        }
    }

    override val ids: Set<Concierge.Id>
        get() =
            mutableSetOf<Concierge.Id>()
}
