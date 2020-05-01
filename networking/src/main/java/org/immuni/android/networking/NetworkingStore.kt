package org.immuni.android.networking

import android.content.Context
import org.immuni.android.extensions.storage.KVStorage

class NetworkingStore(val context: Context, encrypted: Boolean) {
    companion object {
        const val NAME = "Oracle"
        const val SETTINGS_KEY = "Settings"
    }

    private val kvStorage = KVStorage(NAME, context, encrypted = encrypted)

    fun saveSettings(settings: String) {
        kvStorage.save(SETTINGS_KEY, settings)
    }

    fun loadSettings(): String? {
        return kvStorage.load(SETTINGS_KEY)
    }
}
