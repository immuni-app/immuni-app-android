package com.bendingspoons.theirs

import com.bendingspoons.theirs.fcm.FirebaseFCMConfiguration

/**
 * This is the lib configuration
 * the app injects into [Theirs].
 */
interface TheirsConfiguration {
    fun firebaseFCMConfig(): FirebaseFCMConfiguration
}
