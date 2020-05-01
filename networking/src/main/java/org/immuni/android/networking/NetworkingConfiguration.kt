package org.immuni.android.networking

import okhttp3.CertificatePinner

/**
 * This is the networking configuration the app injects into this module
 * in order to customize it.
 */
interface NetworkingConfiguration {
    fun endpoint(): String
    fun showForceUpdate(minVersionCode: Int)
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
