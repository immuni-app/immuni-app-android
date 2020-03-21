package com.bendingspoons.ascolto.ui.force_update

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.playStore.PlayStoreActions

class ForceUpdateViewModel : ViewModel() {
    fun goToPlayStoreAppDetails(context: Context) {
        PlayStoreActions.goToPlayStoreAppDetails(context)
    }
}
