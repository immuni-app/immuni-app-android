package com.bendingspoons.pico

import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.sesame.Sesame
import okhttp3.CertificatePinner
import java.util.*

interface PicoConfiguration: PicoUserInfoProvider {
    fun endpoint(): String
    fun isDevelopmentDevice(): Boolean
    fun concierge(): ConciergeManager
    fun oracle(): Oracle<out OracleSettings, out OracleMe>
    fun sesame(): Sesame
    fun certificatePinner(): CertificatePinner?
    fun additionalMonetizationInfo(): Map<String, Any>
    fun wasInstalledBeforePico(): Boolean
    fun encryptStore(): Boolean
}
