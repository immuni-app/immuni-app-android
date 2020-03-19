package com.bendingspoons.ascolto

import android.content.Context
import com.bendingspoons.ascolto.api.oracle.model.AscoltoMe
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.secretmenu.SecretMenuConfiguration
import com.bendingspoons.secretmenu.SecretMenuItem
import com.bendingspoons.theirs.Theirs
import org.koin.core.KoinComponent
import org.koin.core.inject

class AscoltoSecretMenuConfiguration(val context: Context): SecretMenuConfiguration, KoinComponent {
    private val concierge: ConciergeManager by inject()
    private val database: AscoltoDatabase by inject()
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val theirs: Theirs by inject()

    override val isDevelopmentDevice = {
        oracle.settings()?.developmentDevices?.contains(concierge.aaid.id) == true
    }

    override fun concierge(): ConciergeManager {
        return concierge
    }

    override fun publicItems(): List<SecretMenuItem> {
        return listOf()
    }

    override fun spoonerItems(): List<SecretMenuItem> {
        return listOf()
    }
}
