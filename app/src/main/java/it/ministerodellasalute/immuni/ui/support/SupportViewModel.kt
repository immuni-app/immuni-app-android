package it.ministerodellasalute.immuni.ui.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class SupportViewModel : ViewModel() {

    // TODO replace livedata with real values.

    val supportPhoneNumber = liveData {
        emit("800 91 24 91")
    }

    val osVersion = liveData {
        emit("Android 9")
    }

    val phoneModel = liveData {
        emit("Samsung Galaxy A41")
    }

    val isExposureNotificationsEnabled = liveData {
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

    val connectionStatus = liveData {
        emit("Wi-Fi")
    }
}
