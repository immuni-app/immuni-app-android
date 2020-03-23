package org.ascolto.onlus.geocrowd19.android

import android.content.Context
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.secretmenu.SecretMenuConfiguration
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.theirs.Theirs
import org.ascolto.onlus.geocrowd19.android.managers.AscoltoNotificationManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoSecretMenuConfiguration(val context: Context): SecretMenuConfiguration, KoinComponent {
    private val concierge: ConciergeManager by inject()
    private val database: AscoltoDatabase by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val theirs: Theirs by inject()
    private val notificationManager: AscoltoNotificationManager by inject()

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
            }){}
        )
    }
}
