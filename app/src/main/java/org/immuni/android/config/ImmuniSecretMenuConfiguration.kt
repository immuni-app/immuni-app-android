package org.immuni.android.config

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import org.immuni.android.base.extensions.toast
import org.immuni.android.base.utils.DeviceUtils
import org.immuni.android.networking.model.ImmuniMe
import org.immuni.android.networking.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.ids.IdsManager
import org.immuni.android.networking.Networking
import org.immuni.android.secretmenu.SecretMenuConfiguration
import org.immuni.android.secretmenu.SecretMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.ImmuniApplication
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.service.ImmuniForegroundService
import org.immuni.android.ui.ble.encounters.BleEncountersDebugActivity
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.welcome.Welcome
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniSecretMenuConfiguration(val context: Context): SecretMenuConfiguration, KoinComponent {
    private val ids: IdsManager by inject()
    private val database: ImmuniDatabase by inject()
    private val networking: Networking<ImmuniSettings, ImmuniMe> by inject()
    private val notificationManager: SurveyNotificationManager by inject()
    private val btIdsManager: BtIdsManager by inject()
    private val onboarding: Onboarding by inject()
    private val setup: Setup by inject()
    private val welcome: Welcome by inject()

    override val isDevelopmentDevice = {
        networking.settings()?.developmentDevices?.contains(ids.backupPersistentId.id) == true
    }

    override fun concierge(): IdsManager {
        return ids
    }

    override fun publicItems(): List<SecretMenuItem> {
        return listOf(
            object : SecretMenuItem("\uD83D\uDC68 User ID", { _, _ ->
                DeviceUtils.copyToClipBoard(context, text = ids.backupPersistentId.id ?: "-")
                toast(
                    context,
                    ids.backupPersistentId.id,
                    Toast.LENGTH_LONG
                )
            }){}
        )
    }

    override fun debuggingItems(): List<SecretMenuItem> {
        return listOf(
            object : SecretMenuItem("\uD83D\uDD14 Schedule a notification in 5 seconds", { _, _ ->
                notificationManager.scheduleMock()
            }){},
            object : SecretMenuItem("ℹ️ Copy current bt_id", { context, config ->
                val value = btIdsManager.getCurrentBtId()
                DeviceUtils.copyToClipBoard(context, text = value?.id ?: "-")
                Toast.makeText(context, value?.id ?: "-", Toast.LENGTH_LONG).show()
            }){},
            object : SecretMenuItem("ℹ️ Distinct bt_id count", { context, config ->
                GlobalScope.launch(Dispatchers.Main) {
                    val value = database.bleContactDao().getAllDistinctBtIdsCount()
                    Toast.makeText(context, "# Devices found: $value", Toast.LENGTH_LONG).show()
                }
            }){},
            object : SecretMenuItem("ℹ️ All bt_id count", { context, config ->
                GlobalScope.launch(Dispatchers.Main) {
                    val value = database.bleContactDao().getAllBtIdsCount()
                    Toast.makeText(context, "# Devices found: $value", Toast.LENGTH_LONG).show()
                }
            }){},
            object : SecretMenuItem("ℹ️ Copy distinct bt_ids found", { context, config ->
                GlobalScope.launch(Dispatchers.Main) {
                    val list = database.bleContactDao().getAllDistinctBtIds()
                    Toast.makeText(context, "# Devices found: ${list.joinToString(separator = ", ")}", Toast.LENGTH_LONG).show()
                }
            }){},
            object : SecretMenuItem("ℹ️ BLE encounters debug", { context, config ->
                val context =
                    ImmuniApplication.appContext
                val intent = Intent(context, BleEncountersDebugActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }){},
            object : SecretMenuItem("❌ Stop foreground service", { context, config ->
                Intent(context, ImmuniForegroundService::class.java).also {
                    it.action = ImmuniForegroundService.Actions.STOP.name
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(it)
                        return@also
                    }
                    context.startService(it)
                }
            }){},
            object : SecretMenuItem("\uD83C\uDF00 Start foreground service", { context, config ->
                Intent(context, ImmuniForegroundService::class.java).also {
                    it.action = ImmuniForegroundService.Actions.START.name
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(it)
                        return@also
                    }
                    context.startService(it)
                }
            }){}
        )
    }
}
