package com.bendingspoons.ascolto

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bendingspoons.ascolto.ui.force_update.ForceUpdateActivity
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.OracleConfiguration
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoOracleConfiguration(val context: Context) : OracleConfiguration, KoinComponent {

    val concierge: ConciergeManager by inject()

    override fun endpoint(): String {
        return context.getString(R.string.oracle_base_url)
    }

    override fun concierge(): ConciergeManager {
        return concierge
    }

    override fun showForceUpdate(minVersionCode: Int) {
        val context = AscoltoApplication.appContext
        Log.d("ForceUpdate", "ForceUpdate! Min version is $minVersionCode")

        val intent = Intent(context, ForceUpdateActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}
