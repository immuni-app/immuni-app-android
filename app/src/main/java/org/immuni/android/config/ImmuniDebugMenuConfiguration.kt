package org.immuni.android.config

import android.content.Context
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.debugmenu.DebugMenuConfiguration
import org.immuni.android.debugmenu.DebugMenuItem
import org.immuni.android.managers.SurveyNotificationManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniDebugMenuConfiguration(val context: Context) : DebugMenuConfiguration, KoinComponent {
    private val database: ImmuniDatabase by inject()
    private val notificationManager: SurveyNotificationManager by inject()

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
            }) {}
        )
    }
}
