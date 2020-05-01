package org.immuni.android.config

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.networking.Networking
import org.immuni.android.debugmenu.DebugMenuConfiguration
import org.immuni.android.debugmenu.DebugMenuItem
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

class ImmuniDebugMenuConfiguration(val context: Context): DebugMenuConfiguration, KoinComponent {
    private val database: ImmuniDatabase by inject()
    private val networking: Networking<ImmuniSettings> by inject()
    private val notificationManager: SurveyNotificationManager by inject()
    private val btIdsManager: BtIdsManager by inject()
    private val onboarding: Onboarding by inject()
    private val setup: Setup by inject()
    private val welcome: Welcome by inject()

    override val isDevelopmentDevice = {
        true
    }

    override fun publicItems(): List<DebugMenuItem> {
        return listOf()
    }

    override fun debuggingItems(): List<DebugMenuItem> {
        return listOf(
            object : DebugMenuItem("\uD83D\uDD14 Schedule a notification in 5 seconds", { _, _ ->
                notificationManager.scheduleMock()
            }){},
            object : DebugMenuItem("ℹ️ Copy current bt_id", { context, config ->
                val value = btIdsManager.getCurrentBtId()
                DeviceUtils.copyToClipBoard(context, text = value?.id ?: "-")
                Toast.makeText(context, value?.id ?: "-", Toast.LENGTH_LONG).show()
            }){},
            object : DebugMenuItem("ℹ️ Distinct bt_id count", { context, config ->
                GlobalScope.launch(Dispatchers.Main) {
                    val value = database.bleContactDao().getAllDistinctBtIdsCount()
                    Toast.makeText(context, "# Devices found: $value", Toast.LENGTH_LONG).show()
                }
            }){},
            object : DebugMenuItem("ℹ️ All bt_id count", { context, config ->
                GlobalScope.launch(Dispatchers.Main) {
                    val value = database.bleContactDao().getAllBtIdsCount()
                    Toast.makeText(context, "# Devices found: $value", Toast.LENGTH_LONG).show()
                }
            }){},
            object : DebugMenuItem("ℹ️ Copy distinct bt_ids found", { context, config ->
                GlobalScope.launch(Dispatchers.Main) {
                    val list = database.bleContactDao().getAllDistinctBtIds()
                    Toast.makeText(context, "# Devices found: ${list.joinToString(separator = ", ")}", Toast.LENGTH_LONG).show()
                }
            }){},
            object : DebugMenuItem("ℹ️ BLE encounters debug", { context, config ->
                val context =
                    ImmuniApplication.appContext
                val intent = Intent(context, BleEncountersDebugActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }){},
            object : DebugMenuItem("❌ Stop foreground service", { context, config ->
                Intent(context, ImmuniForegroundService::class.java).also {
                    it.action = ImmuniForegroundService.Actions.STOP.name
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(it)
                        return@also
                    }
                    context.startService(it)
                }
            }){},
            object : DebugMenuItem("\uD83C\uDF00 Start foreground service", { context, config ->
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
