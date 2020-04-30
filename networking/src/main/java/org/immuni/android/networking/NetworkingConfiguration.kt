package org.immuni.android.networking

import okhttp3.CertificatePinner

interface NetworkingConfiguration {
    fun endpoint(): String
    fun showForceUpdate(minVersionCode: Int)
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
