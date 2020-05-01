package org.immuni.android.api

import org.immuni.android.api.model.ImmuniSettings

interface APIListener {
    suspend fun onSettingsUpdate(settings: ImmuniSettings)
}