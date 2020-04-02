package org.immuni.android

import android.content.Context
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.pico.PicoConfiguration
import com.bendingspoons.sesame.Sesame
import okhttp3.CertificatePinner
import org.immuni.android.picoMetrics.PushPermissionLevel
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniPicoConfiguration(val context: Context): PicoConfiguration, KoinComponent {

    val concierge: ConciergeManager by inject()
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()

    override fun endpoint(): String {
        return context.getString(R.string.pico_base_url)
    }

    override fun isDevelopmentDevice(): Boolean {
        return oracle.settings()?.developmentDevices?.contains(concierge.aaid.id) == true
    }

    override fun concierge(): ConciergeManager {
        return concierge
    }

    override fun oracle(): Oracle<out OracleSettings, out OracleMe> {
        return oracle
    }

    override fun sesame(): Sesame {
        return Sesame(PicoSesameConfiguration())
    }

    override fun additionalMonetizationInfo(): Map<String, Any> {
        return mapOf()
    }

    override fun wasInstalledBeforePico() = false

    override fun encryptStore() = true

    override fun certificatePinner(): CertificatePinner? {
        return CertificatePinner.Builder()
            .add(
                "*.ascolto-onlus.org",
                "sha256/ArtgUpVapq77kY3upbiBWyWQMfSo1ilJ1z6W0UR2SLQ="
            ).build()
    }

    override val userInfo: Map<String, Any>
        get() = mapOf(
            PushPermissionLevel.instance().userInfo()
        )
}
