package org.immuni.android.ui.welcome

import org.immuni.android.extensions.storage.KVStorage
import org.koin.core.KoinComponent

class Welcome(private val kvStorage: KVStorage): KoinComponent {

    fun isComplete(): Boolean {
        return kvStorage.load<Boolean>(WELCOME_COMPLETE) == true
    }

    fun setCompleted(complete: Boolean) {
        kvStorage.save(WELCOME_COMPLETE, complete)
    }

    companion object {
        const val TAG = "Welcome"
        private const val WELCOME_COMPLETE = "WELCOME_COMPLETE"
    }
}
