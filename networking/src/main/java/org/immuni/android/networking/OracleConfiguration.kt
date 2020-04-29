package org.immuni.android.networking

import org.immuni.android.ids.ConciergeManager
import okhttp3.CertificatePinner

interface OracleConfiguration {
    fun endpoint(): String
    fun concierge(): ConciergeManager
    fun showForceUpdate(minVersionCode: Int)
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
