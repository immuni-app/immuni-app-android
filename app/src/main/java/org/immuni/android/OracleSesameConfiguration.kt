package org.immuni.android

import com.bendingspoons.sesame.SesameConfiguration

class OracleSesameConfiguration: SesameConfiguration {
    override val secretKey: String = BuildConfig.sesameOracleKey
}
