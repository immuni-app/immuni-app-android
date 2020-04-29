package org.immuni.android.analytics

import org.immuni.android.ids.ConciergeManager
import org.immuni.android.networking.Oracle
import org.immuni.android.networking.api.model.OracleMe
import org.immuni.android.networking.api.model.OracleSettings
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
