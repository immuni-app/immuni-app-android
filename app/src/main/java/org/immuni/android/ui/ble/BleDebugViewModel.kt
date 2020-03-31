package org.immuni.android.ui.ble

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.fraunhofer.iis.DistanceEstimate
import de.fraunhofer.iis.Estimator
import de.fraunhofer.iis.Measurement
import de.fraunhofer.iis.ModelProvider
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class BleDebugViewModel : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val estimator: Estimator by inject()

    val distances = MutableLiveData<List<DistanceEstimate>>()

    init {
        startTimer()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun startTimer() {
        uiScope.launch {
            repeat(Int.MAX_VALUE) {
                val list =
                    estimator.push(Measurement(0, 0f, "", "", ModelProvider.MOBILE_DEVICE.DEFAULT))
                distances.value = list.filter { it.deviceId1.isNotEmpty() && it.deviceId2.isNotEmpty() }
                delay(1000)
            }
        }
    }
}
