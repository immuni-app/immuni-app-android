package org.ascolto.onlus.geocrowd19.android.ui.setup

import org.ascolto.onlus.geocrowd19.android.db.DATABASE_VERSION
import org.ascolto.onlus.geocrowd19.android.util.isFlagSet
import org.ascolto.onlus.geocrowd19.android.util.setFlag
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
