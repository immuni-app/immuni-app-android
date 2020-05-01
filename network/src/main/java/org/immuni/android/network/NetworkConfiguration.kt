package org.immuni.android.network

import okhttp3.CertificatePinner

/**
 * This is the networking configuration the app injects into this module
 * in order to customize it.
 */
interface NetworkConfiguration {
    fun endpoint(): String
    fun certificatePinner(): CertificatePinner?
}
