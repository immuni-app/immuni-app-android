package org.immuni.android.ui.onboarding

import org.immuni.android.extensions.storage.KVStorage
import org.koin.core.KoinComponent

class Onboarding(private val kvStorage: KVStorage): KoinComponent {

    fun isComplete(): Boolean {
        return kvStorage.load<Boolean>(ONBOARDING_COMPLETE) == true
    }

    fun setCompleted(complete: Boolean) {
        kvStorage.save(ONBOARDING_COMPLETE, complete)
    }

    fun familyDialogShown(): Boolean {
        return kvStorage.load<Boolean>(FAMILY_DIALOG_SHOWN) == true
    }

    fun setFamilyDialogShown(complete: Boolean) {
        kvStorage.save(FAMILY_DIALOG_SHOWN, complete)
    }

    companion object {
        const val TAG = "Onboarding"
        private const val ONBOARDING_COMPLETE = "ONBOARDING_COMPLETE"
        private const val FAMILY_DIALOG_SHOWN = "FAMILY_DIALOG_SHOWN"
    }
}
