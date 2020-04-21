package org.immuni.android.config

import com.bendingspoons.sesame.SesameConfiguration
import org.immuni.android.BuildConfig

class OracleSesameConfiguration: SesameConfiguration {
    override val secretKey: String =
        BuildConfig.sesameOracleKey
}
