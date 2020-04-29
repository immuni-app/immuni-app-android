package org.immuni.android.networking

import org.immuni.android.ids.IdsManager
import okhttp3.CertificatePinner

interface NetworkingConfiguration {
    fun endpoint(): String
    fun idsManager(): IdsManager
    fun showForceUpdate(minVersionCode: Int)
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
