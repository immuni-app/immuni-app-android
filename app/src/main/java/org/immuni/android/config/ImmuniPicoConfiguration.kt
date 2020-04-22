package org.immuni.android.config

import android.content.Context
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.pico.PicoConfiguration
import com.bendingspoons.sesame.Sesame
import okhttp3.CertificatePinner
import org.immuni.android.R
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.metrics.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class ImmuniPicoConfiguration(val context: Context): PicoConfiguration, KoinComponent {

    val concierge: ConciergeManager by inject()
    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val database: ImmuniDatabase by inject()

    override fun endpoint(): String {
        return context.getString(R.string.pico_base_url)
    }

    override fun isDevelopmentDevice(): Boolean {
        return oracle.settings()?.developmentDevices?.contains(concierge.backupPersistentId.id) == true
    }

    override fun concierge(): ConciergeManager {
        return concierge
    }

    override fun oracle(): Oracle<out OracleSettings, out OracleMe> {
        return oracle
    }

    override fun sesame(): Sesame {
        return Sesame(PicoSesameConfiguration())
    }

    override fun encryptStore() = true

    override fun certificatePinner(): CertificatePinner? {
        return CertificatePinner.Builder()
            .add(
                "*.pilot1.immuni.org",
                "sha256/0LuJrVCJTXlR2mn2sLP0p23hNIaVcxdAr62LYNvnsaY="
            )
            .add(
                "*.ascolto-onlus.org",
                "sha256/ArtgUpVapq77kY3upbiBWyWQMfSo1ilJ1z6W0UR2SLQ="
            ).build()
    }

    override val userInfo: Map<String, Any>
        get() = mapOf(
            PicoUserInfos.pushPermissionLevel(),
            PicoUserInfos.bluetoothActive(),
            PicoUserInfos.locationPermissionsLevel(),
            PicoUserInfos.locationActive(context),
            PicoUserInfos.batteryOptimization(context),
            PicoUserInfos.batteryLevel(context),
            PicoUserInfos.databaseSize(context)
        )
}
