package org.immuni.android

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bendingspoons.base.utils.DeviceUtils
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.secretmenu.SecretMenuConfiguration
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.theirs.Theirs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.managers.BtIdsManager
import org.immuni.android.ui.ble.encounters.BleEncountersDebugActivity
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.welcome.Welcome
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniSecretMenuConfiguration(val context: Context): SecretMenuConfiguration, KoinComponent {
    private val concierge: ConciergeManager by inject()
    private val database: ImmuniDatabase by inject()
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val theirs: Theirs by inject()
    private val notificationManager: SurveyNotificationManager by inject()
    private val btIdsManager: BtIdsManager by inject()
    private val onboarding: Onboarding by inject()
    private val setup: Setup by inject()
    private val welcome: Welcome by inject()

    override val isDevelopmentDevice = {
        oracle.settings()?.developmentDevices?.contains(concierge.backupPersistentId.id) == true
    }

    override fun concierge(): ConciergeManager {
        return concierge
    }

    override fun publicItems(): List<SecretMenuItem> {
        return listOf()
    }

    override fun spoonerItems(): List<SecretMenuItem> {
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
                val context = ImmuniApplication.appContext
                val intent = Intent(context, BleEncountersDebugActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }){}
        )
    }
}
