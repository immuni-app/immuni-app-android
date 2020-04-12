package com.bendingspoons.pico.install

import android.content.Context
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.pico.PicoConfiguration
import com.bendingspoons.pico.PicoEventManager
import com.bendingspoons.pico.PicoInstallInfo
import com.bendingspoons.pico.model.Install
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

internal class PicoInstallManager(
    context: Context,
    config: PicoConfiguration,
    private val eventManager: CompletableDeferred<PicoEventManager>
) {
    companion object {
        const val appVersionKey = "appVersionKey"
        const val bundleVersionKey = "bundleVersionKey"
        const val firstInstallTimeMillisKey = "firstInstallTimeMillisKey"
        const val lastInstallTimeMillisKey = "lastInstallTimeMillisKey"
        const val wasInstalledBeforePicoKey = "wasInstalledBeforePicoKey"
    }

    private val storage = KVStorage(
        "PICO_INSTALL_MANAGER",
        context,
        encrypted = config.encryptStore()
    )

    val info: PicoInstallInfo

    init {
        val concierge = config.concierge()

        val firstInstallDate =
            storage.load<Long>(firstInstallTimeMillisKey)?.let { Date(it) } ?: Date()
        val lastInstallDate =
            storage.load<Long>(lastInstallTimeMillisKey)?.let { Date(it) } ?: Date()

        val oldAppVersion = storage.load<String>(appVersionKey)
        val oldBundleVersion = storage.load<String>(bundleVersionKey)

        val wasInstalledBeforePico = storage.load(wasInstalledBeforePicoKey)
            ?: config.wasInstalledBeforePico()

        info = PicoInstallInfo(
            firstInstallDate = firstInstallDate,
            lastInstallDate = lastInstallDate,
            wasInstalledBeforePico = wasInstalledBeforePico
        )

        val installEvent = computeInstallEvent(
            backupPersistentId = concierge.backupPersistentId,
            nonBackupPersistentId = concierge.nonBackupPersistentId,
            wasInstalledBeforePico = wasInstalledBeforePico,
            currentAppVersion = DeviceUtils.appVersionName(context),
            oldAppVersion = oldAppVersion,
            oldBundleVersion = oldBundleVersion
        )

        if (!storage.contains(firstInstallTimeMillisKey)) {
            storage.save(firstInstallTimeMillisKey, firstInstallDate.time)
        }
        if (!storage.contains(wasInstalledBeforePicoKey)) {
            storage.save(wasInstalledBeforePicoKey, wasInstalledBeforePico)
        }
        storage.save(appVersionKey, DeviceUtils.appVersionName(context))
        storage.save(bundleVersionKey, DeviceUtils.appVersionCode(context).toString())

        if (installEvent != null) {
            storage.save(lastInstallTimeMillisKey, lastInstallDate.time)

            GlobalScope.launch {
                sendInstall(installEvent)
            }
        }
    }

    private fun computeInstallEvent(
        backupPersistentId: Concierge.Id,
        nonBackupPersistentId: Concierge.Id,
        wasInstalledBeforePico: Boolean,
        currentAppVersion: String,
        oldAppVersion: String?,
        oldBundleVersion: String?
    ): PicoInstallEventData? {
        if (backupPersistentId.creation == Concierge.CreationType.readFromFile &&
            nonBackupPersistentId.creation == Concierge.CreationType.readFromFile &&
            oldAppVersion == currentAppVersion
        ) {
            return null
        }

        return PicoInstallEventData(
            backupPersistentIdStatus = backupPersistentId.creation,
            nonBackupPersistentIdStatus = nonBackupPersistentId.creation,
            installedBeforePico = wasInstalledBeforePico,
            newAppVersion = currentAppVersion,
            oldAppVersion = oldAppVersion,
            oldBundleVersion = oldBundleVersion
        )
    }

    private suspend fun sendInstall(installEventData: PicoInstallEventData) {
        eventManager.await().trackEvent(Install(installEventData))
    }
}
