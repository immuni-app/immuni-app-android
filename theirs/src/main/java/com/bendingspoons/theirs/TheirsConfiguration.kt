package com.bendingspoons.theirs

import com.bendingspoons.theirs.fcm.FirebaseFCMConfiguration

interface TheirsConfiguration {
    fun firebaseFCMConfig(): FirebaseFCMConfiguration
}
