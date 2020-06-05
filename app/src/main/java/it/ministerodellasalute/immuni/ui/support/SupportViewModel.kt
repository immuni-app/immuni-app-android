package it.ministerodellasalute.immuni.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class SupportViewModel : ViewModel() {

    // TODO replace livedata with real values.

    val contactSupportPhone = liveData {
        emit("800 91 24 91")
    }

    val supportWorkingHours = liveData {
        emit(7 to 22)
    }

    val osVersion = liveData {
        emit("Android 9")
    }

    val deviceModel = liveData {
        emit("Samsung Galaxy A41")
    }

    val isExposureNotificationEnabled = liveData {
        emit("Attiva")
    }

    val isBluetoothEnabled = liveData {
        emit("Attivo")
    }

    val appVersion = liveData {
        emit("1.0.0 (23)")
    }
    val googlePlayVersion = liveData {
        emit("1.17.20")
    }

    val connectionType = liveData {
        emit("Wi-Fi")
    }
}
