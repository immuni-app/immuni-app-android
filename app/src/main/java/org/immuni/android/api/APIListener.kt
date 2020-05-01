package org.immuni.android.api

import org.immuni.android.api.model.ImmuniSettings

/**
 * This listener will be notified through [onSettingsUpdate]
 * when new settings are fetched from the network.
 */
interface APIListener {
    suspend fun onSettingsUpdate(settings: ImmuniSettings)
}