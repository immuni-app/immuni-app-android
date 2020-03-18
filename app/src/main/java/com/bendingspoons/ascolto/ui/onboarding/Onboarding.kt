package com.bendingspoons.ascolto.ui.onboarding

import com.bendingspoons.ascolto.util.isFlagSet
import com.bendingspoons.ascolto.util.setFlag
import org.koin.core.KoinComponent

class Onboarding: KoinComponent {

    fun isComplete(): Boolean {
        return isFlagSet(ONBOARDING_COMPLETE)
    }

    fun setCompleted(complete: Boolean) {
        setFlag(ONBOARDING_COMPLETE, complete)
    }

    companion object {
        const val TAG = "Onboarding"
        private const val ONBOARDING_COMPLETE = "ONBOARDING_COMPLETE"
    }
}