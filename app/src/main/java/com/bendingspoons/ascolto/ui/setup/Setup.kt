package com.bendingspoons.ascolto.ui.setup

import com.bendingspoons.ascolto.db.DATABASE_VERSION
import com.bendingspoons.ascolto.util.isFlagSet
import com.bendingspoons.ascolto.util.setFlag
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