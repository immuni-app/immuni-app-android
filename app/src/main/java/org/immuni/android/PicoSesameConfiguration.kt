package org.immuni.android

import android.content.Context
import com.bendingspoons.sesame.SesameConfiguration

class PicoSesameConfiguration(context: Context): SesameConfiguration {
    override val secretKey = context.resources.getString(R.string.sesami_pico_key)
}
