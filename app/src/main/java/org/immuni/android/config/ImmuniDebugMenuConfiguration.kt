package org.immuni.android.config

import android.content.Context
import android.content.Intent
import android.os.Build
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.debugmenu.DebugMenuConfiguration
import org.immuni.android.debugmenu.DebugMenuItem
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.welcome.Welcome
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniDebugMenuConfiguration(val context: Context): DebugMenuConfiguration, KoinComponent {
    private val database: ImmuniDatabase by inject()
    private val notificationManager: SurveyNotificationManager by inject()
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
            }){}
        )
    }
}
