package org.immuni.android.config

import android.content.Context
import okhttp3.CertificatePinner
import org.immuni.android.R
import org.immuni.android.network.NetworkConfiguration
import org.koin.core.KoinComponent

class NetworkExposureInjestionConfiguration(val context: Context) : NetworkConfiguration, KoinComponent {

    override fun endpoint(): String {
        return context.getString(R.string.oracle_base_url)
    }

    override fun certificatePinner(): CertificatePinner? {
        return CertificatePinner.Builder()
            .add(
                "*.ascolto-onlus.org",
                "sha256/OgRj8cQiFRmMxno/YNLtTKsHIJfP+EacJiMfoRfpLe8="
            ).build()
    }
}
