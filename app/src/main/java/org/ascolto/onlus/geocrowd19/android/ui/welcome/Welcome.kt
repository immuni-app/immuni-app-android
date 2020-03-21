package org.ascolto.onlus.geocrowd19.android.ui.welcome

import org.ascolto.onlus.geocrowd19.android.util.isFlagSet
import org.ascolto.onlus.geocrowd19.android.util.setFlag
import org.koin.core.KoinComponent

class Welcome: KoinComponent {

    fun isComplete(): Boolean {
        return isFlagSet(WELCOME_COMPLETE)
    }

    fun setCompleted(complete: Boolean) {
        setFlag(WELCOME_COMPLETE, complete)
    }

    companion object {
        const val TAG = "Welcome"
        private const val WELCOME_COMPLETE = "WELCOME_COMPLETE"
    }
}