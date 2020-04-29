package org.immuni.android.analytics.userconsent

import android.content.Context
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.analytics.PicoConfiguration

// Users can opt-in or out from sending analytics events

enum class UserConsentLevel {
    UNKNOWN, ACCEPTED, DENIED
}

internal class UserConsent(context: Context,
                           config: PicoConfiguration,
                           private val store: UserConsentStore = UserConsentStore(KVStorage(
                      "PICO_USER_CONSENT",
                      context,
                      encrypted = config.encryptStore()
                  ))) {

    private val levelKey = "level"

    var level: UserConsentLevel
        get() = store.load(levelKey)
        set(value) {
            store.save(levelKey, value)
        }
}

// internal store wrapper class to allow unit tests
// since KVStorage internal reified methods cannot be mocked with Mockk.

internal class UserConsentStore(val storage: KVStorage) {
    fun load(key: String): UserConsentLevel {
        return storage.load(key) ?: UserConsentLevel.UNKNOWN
    }

    fun save(key: String, level: UserConsentLevel) {
        storage.save(key, level)
    }
}

