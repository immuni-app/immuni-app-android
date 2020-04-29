package org.immuni.android.config

import android.content.Context
import org.immuni.android.ids.IdsManager
import org.immuni.android.networking.Networking
import org.immuni.android.networking.api.model.NetworkingMe
import org.immuni.android.networking.api.model.NetworkingSettings
import org.immuni.android.analytics.PicoConfiguration
import okhttp3.CertificatePinner
import org.immuni.android.R
import org.immuni.android.networking.model.ImmuniMe
import org.immuni.android.networking.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.metrics.*
import org.koin.core.KoinComponent
import org.koin.core.inject


class ImmuniPicoConfiguration(val context: Context): PicoConfiguration, KoinComponent {

    val ids: IdsManager by inject()
    val networking: Networking<ImmuniSettings, ImmuniMe> by inject()
    val database: ImmuniDatabase by inject()

    override fun endpoint(): String {
        return context.getString(R.string.pico_base_url)
    }

    override fun isDevelopmentDevice(): Boolean {
        return networking.settings()?.developmentDevices?.contains(ids.backupPersistentId.id) == true
    }

    override fun idsManager(): IdsManager {
        return ids
    }

    override fun oracle(): Networking<out NetworkingSettings, out NetworkingMe> {
        return networking
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
            PicoUserInfos.databaseSize(context),
            PicoUserInfos.bleIsAdvertising(),
            PicoUserInfos.bleAdvertiseMode(),
            PicoUserInfos.bleTxPowerLevel(),
            PicoUserInfos.bleScanMode()
        )
}
