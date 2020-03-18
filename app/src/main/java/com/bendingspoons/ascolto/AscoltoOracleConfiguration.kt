package com.bendingspoons.ascolto

import android.content.Context
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.OracleConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoOracleConfiguration(val context: Context) : OracleConfiguration<AscoltoSettings>, KoinComponent {

    val concierge: ConciergeManager by inject()

    override fun endpoint(): String {
        return context.getString(R.string.oracle_base_url)
    }

    override fun concierge(): ConciergeManager {
        return concierge
    }
}
