package org.immuni.android.ui.setup

import org.immuni.android.db.DATABASE_VERSION
import org.immuni.android.util.isFlagSet
import org.immuni.android.util.setFlag
import org.koin.core.KoinComponent

class Setup: KoinComponent {

    fun isComplete(): Boolean {
        return isFlagSet(SETUP_COMPLETE)
    }

    fun setCompleted(complete: Boolean) {
        setFlag(SETUP_COMPLETE, complete)
    }

    companion object {
        const val TAG = "Setup"
        private const val SETUP_COMPLETE = "SETUP_COMPLETE_$DATABASE_VERSION"
    }
}
