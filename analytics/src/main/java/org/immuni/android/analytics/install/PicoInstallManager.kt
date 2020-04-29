package org.immuni.android.analytics.install

import android.content.Context
import org.immuni.android.ids.Ids
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.analytics.PicoConfiguration
import org.immuni.android.analytics.PicoEventManager
import org.immuni.android.analytics.PicoInstallInfo
import org.immuni.android.analytics.model.Install
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
    }

    private val storage = KVStorage(
        "PICO_INSTALL_MANAGER",
        context,
        encrypted = config.encryptStore()
    )

    val info: PicoInstallInfo

    init {
        val concierge = config.ids().manager

        val firstInstallDate =
            storage.load<Long>(firstInstallTimeMillisKey)?.let { Date(it) } ?: Date()
        val lastInstallDate =
            storage.load<Long>(lastInstallTimeMillisKey)?.let { Date(it) } ?: Date()

        val oldAppVersion = storage.load<String>(appVersionKey)
        val oldBundleVersion = storage.load<String>(bundleVersionKey)

        info = PicoInstallInfo(
            firstInstallDate = firstInstallDate,
            lastInstallDate = lastInstallDate
        )

        val installEvent = computeInstallEvent(
            backupPersistentId = concierge.id,
            nonBackupPersistentId = concierge.id,
            currentAppVersion = DeviceUtils.appVersionName(context),
            oldAppVersion = oldAppVersion,
            oldBundleVersion = oldBundleVersion
        )

        if (!storage.contains(firstInstallTimeMillisKey)) {
            storage.save(firstInstallTimeMillisKey, firstInstallDate.time)
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
        backupPersistentId: Ids.Id,
        nonBackupPersistentId: Ids.Id,
        currentAppVersion: String,
        oldAppVersion: String?,
        oldBundleVersion: String?
    ): PicoInstallEventData? {
        if (backupPersistentId.creation == Ids.CreationType.readFromFile &&
            nonBackupPersistentId.creation == Ids.CreationType.readFromFile &&
            oldAppVersion == currentAppVersion
        ) {
            return null
        }

        return PicoInstallEventData(
            backupPersistentIdStatus = backupPersistentId.creation,
            nonBackupPersistentIdStatus = nonBackupPersistentId.creation,
            newAppVersion = currentAppVersion,
            oldAppVersion = oldAppVersion,
            oldBundleVersion = oldBundleVersion
        )
    }

    private suspend fun sendInstall(installEventData: PicoInstallEventData) {
        eventManager.await().trackEvent(Install(installEventData))
    }
}
