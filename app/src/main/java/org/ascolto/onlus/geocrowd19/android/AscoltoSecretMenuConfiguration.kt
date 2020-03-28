package org.ascolto.onlus.geocrowd19.android

import android.content.Context
import android.widget.Toast
import com.bendingspoons.base.utils.DeviceUtils
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.secretmenu.SecretMenuConfiguration
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.secretmenu.ui.ExitActivity
import com.bendingspoons.theirs.Theirs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ascolto.onlus.geocrowd19.android.managers.AscoltoNotificationManager
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.Onboarding
import org.ascolto.onlus.geocrowd19.android.ui.setup.Setup
import org.ascolto.onlus.geocrowd19.android.ui.welcome.Welcome
import org.ascolto.onlus.geocrowd19.android.util.Flags
import org.ascolto.onlus.geocrowd19.android.util.setFlag
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoSecretMenuConfiguration(val context: Context): SecretMenuConfiguration, KoinComponent {
    private val concierge: ConciergeManager by inject()
    private val database: AscoltoDatabase by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val theirs: Theirs by inject()
    private val notificationManager: AscoltoNotificationManager by inject()
    private val onboarding: Onboarding by inject()
    private val setup: Setup by inject()
    private val welcome: Welcome by inject()

    override val isDevelopmentDevice = {
        true
        //oracle.settings()?.developmentDevices?.contains(concierge.aaid.id) == true
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
            object : SecretMenuItem("ℹ️ Copy bt_id", { context, config ->
                val value = oracle.me()?.btId ?: ""
                DeviceUtils.copyToClipBoard(context, text = value)
                Toast.makeText(context, value, Toast.LENGTH_LONG).show()
            }){},
            object : SecretMenuItem("\uD83D\uDCA5 Clear Immuni", { context, config ->
                config.concierge().resetUserIds()
                onboarding.setCompleted(false)
                setup.setCompleted(false)
                welcome.setCompleted(false)
                val flag = Flags.ADD_FAMILY_MEMBER_DIALOG_SHOWN
                setFlag(flag, false)
                ExitActivity.exitApplication(context)
            }){},
            object : SecretMenuItem("ℹ️ Distinct bt_id count", { context, config ->
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
            }){}
        )
    }
}
