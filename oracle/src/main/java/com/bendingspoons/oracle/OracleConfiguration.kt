package com.bendingspoons.oracle

import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.sesame.Sesame
import okhttp3.CertificatePinner
import java.util.*
import kotlin.reflect.KClass

interface OracleConfiguration {
    fun endpoint(): String
    fun concierge(): ConciergeManager
    fun showForceUpdate(minVersionCode: Int)
    fun sesame(): Sesame?
    fun certificatePinner(): CertificatePinner?
    fun encryptStore(): Boolean
}
