package com.bendingspoons.oracle

import android.content.Context
import com.bendingspoons.base.storage.KVStorage

class OracleStore(val context: Context, encrypted: Boolean) {
    companion object {
        const val NAME = "Oracle"
        const val SETTINGS_KEY = "Settings"
        const val ME_KEY = "Me"
    }

    private val kvStorage = KVStorage(NAME, context, encrypted = encrypted)

    fun saveSettings(settings: String) {
        kvStorage.save(SETTINGS_KEY, settings)
    }

    fun loadSettings(): String? {
        return kvStorage.load(SETTINGS_KEY)
    }

    fun loadMe(): String? {
        return kvStorage.load(ME_KEY)
    }

    fun saveMe(me: String) {
        kvStorage.save(ME_KEY, me)
    }
}
