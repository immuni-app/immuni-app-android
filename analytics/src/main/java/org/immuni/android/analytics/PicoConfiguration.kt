package org.immuni.android.analytics

import org.immuni.android.ids.IdsManager
import org.immuni.android.networking.Networking
import org.immuni.android.networking.api.model.NetworkingMe
import org.immuni.android.networking.api.model.NetworkingSettings
import okhttp3.CertificatePinner

/**
 * This is the lib configuration
 * the app injects into [Pico].
 */
interface PicoConfiguration: PicoUserInfoProvider {
    fun endpoint(): String
    fun isDevelopmentDevice(): Boolean
    fun idsManager(): IdsManager
    fun oracle(): Networking<out NetworkingSettings, out NetworkingMe>
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
