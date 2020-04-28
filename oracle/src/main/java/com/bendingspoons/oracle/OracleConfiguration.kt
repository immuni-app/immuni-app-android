package com.bendingspoons.oracle

import com.bendingspoons.concierge.ConciergeManager
import okhttp3.CertificatePinner

interface OracleConfiguration {
    fun endpoint(): String
    fun concierge(): ConciergeManager
    fun showForceUpdate(minVersionCode: Int)
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
