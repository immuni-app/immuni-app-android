package com.bendingspoons.ascolto

import android.content.Context
import com.bendingspoons.ascolto.api.oracle.model.AscoltoMe
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.pico.PicoConfiguration
import com.bendingspoons.theirs.adjust.Adjust
import com.bendingspoons.sesame.Sesame
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoPicoConfiguration(val context: Context): PicoConfiguration, KoinComponent {

    val concierge: ConciergeManager by inject()
    val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    val sesame: Sesame by inject()
    val adjust: Adjust by inject()

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
        return sesame
    }

    override fun additionalMonetizationInfo(): Map<String, Any> {
        return mapOf()
    }

    override fun wasInstalledBeforePico(): Boolean {
        return false
    }

    override val userInfo: Map<String, Any>
        get() = mapOf()
}
