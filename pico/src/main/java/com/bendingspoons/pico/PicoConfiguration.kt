package com.bendingspoons.pico

import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import okhttp3.CertificatePinner

/**
 * This is the lib configuration
 * the app injects into [Pico].
 */
interface PicoConfiguration: PicoUserInfoProvider {
    fun endpoint(): String
    fun isDevelopmentDevice(): Boolean
    fun concierge(): ConciergeManager
    fun oracle(): Oracle<out OracleSettings, out OracleMe>
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
