package org.immuni.android.config

import com.bendingspoons.sesame.SesameConfiguration
import org.immuni.android.BuildConfig

class PicoSesameConfiguration: SesameConfiguration {
    override val secretKey: String = BuildConfig.sesamePicoKey
}
