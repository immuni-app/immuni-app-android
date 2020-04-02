package org.immuni.android

import android.content.Context
import android.content.Intent
import android.util.Log
import org.immuni.android.ui.force_update.ForceUpdateActivity
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.OracleConfiguration
import com.bendingspoons.sesame.Sesame
import okhttp3.CertificatePinner
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoOracleConfiguration(val context: Context) : OracleConfiguration, KoinComponent {

    val concierge: ConciergeManager by inject()
    private val sesame = Sesame(OracleSesameConfiguration())

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

    override fun sesame(): Sesame? {
        return sesame
    }

    override fun certificatePinner(): CertificatePinner? {
        return CertificatePinner.Builder()
            .add(
            "*.ascolto-onlus.org",
            "sha256/OgRj8cQiFRmMxno/YNLtTKsHIJfP+EacJiMfoRfpLe8="
            ).build()
    }

    override fun encryptStore() = false
}
