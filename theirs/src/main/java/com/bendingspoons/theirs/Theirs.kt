package com.bendingspoons.theirs

import android.content.Context
import com.bendingspoons.theirs.fcm.FirebaseFCM

/**
 * Theirs lib contains third party libraries.
 *
 * @param context
 * @param config the lib configuration injected by the app.
 *
 */
class Theirs(context: Context, config: TheirsConfiguration) {

    init {
        Theirs.config = config

        // Firebase FCM
        FirebaseFCM(context, config.firebaseFCMConfig())
    }
    
    companion object {
        lateinit var config: TheirsConfiguration
    }
}
