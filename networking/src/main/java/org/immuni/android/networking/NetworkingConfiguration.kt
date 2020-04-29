package org.immuni.android.networking

import okhttp3.CertificatePinner
import org.immuni.android.ids.Ids

interface NetworkingConfiguration {
    fun endpoint(): String
    fun ids(): Ids
    fun showForceUpdate(minVersionCode: Int)
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
