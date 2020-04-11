package org.immuni.android.ui.ble.encounters

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.fraunhofer.iis.DistanceEstimate
import de.fraunhofer.iis.Estimator
import de.fraunhofer.iis.Measurement
import de.fraunhofer.iis.ModelProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.BLEContactEntity
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class BleEncountersDebugViewModel : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val database: ImmuniDatabase by inject()

    private val encounters = database.bleContactDao().getAllFlow()
    val listModel = MutableLiveData<List<EncountersItem>>()
    val lastEncounter = MutableLiveData<BLEContactEntity>()

    init {
        uiScope.launch {
            encounters
                .collect {
                    it.lastOrNull()?.let {
                        lastEncounter.value = it
                    }

                    val list = withContext(Dispatchers.Default) {
                        it.asSequence().groupingBy { encounter ->
                            val calendar = Calendar.getInstance().apply {
                                time = encounter.timestamp
                            }
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            val minutes = when (calendar.get(Calendar.MINUTE)) {
                                in 1..15 -> "00/15"
                                in 16..30 -> "15/30"
                                in 31..45 -> "30/45"
                                else -> "45/60"
                            }

                            "${day}/${month}/${year} ${hour}:${minutes}"
                        }
                        .eachCount().map { item ->
                            EncountersItem(item.key, item.value)
                        }.sortedBy {
                            it.timeWindows
                        }
                    }
                    listModel.value = list
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}

data class EncountersItem(
    val timeWindows: String,
    val encounters: Int
)
