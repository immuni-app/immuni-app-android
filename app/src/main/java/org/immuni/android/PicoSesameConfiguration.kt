package org.immuni.android

import com.bendingspoons.sesame.SesameConfiguration

class PicoSesameConfiguration: SesameConfiguration {
    override val secretKey: String = BuildConfig.sesamePicoKey
}
