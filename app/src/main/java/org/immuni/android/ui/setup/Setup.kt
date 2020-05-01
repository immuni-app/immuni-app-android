package org.immuni.android.ui.setup

import org.immuni.android.db.DATABASE_VERSION
import org.immuni.android.extensions.storage.KVStorage
import org.koin.core.KoinComponent

class Setup(private val kvStorage: KVStorage): KoinComponent {

    fun isComplete(): Boolean {
        return kvStorage.load<Boolean>(SETUP_COMPLETE) == true
    }

    fun setCompleted(complete: Boolean) {
        kvStorage.save(SETUP_COMPLETE, complete)
    }

    companion object {
        private const val SETUP_COMPLETE = "SETUP_COMPLETE_$DATABASE_VERSION"
    }
}
