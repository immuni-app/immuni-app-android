package com.bendingspoons.ascolto.ui.welcome

import com.bendingspoons.ascolto.util.isFlagSet
import com.bendingspoons.ascolto.util.setFlag
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