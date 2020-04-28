package org.immuni.android.ui.ble.encounters

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.db.entity.BLEContactEntity
import org.immuni.android.db.entity.relativeTimestampToDate
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.DateFormat
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
                .collect { list ->
                    list.lastOrNull()?.let { last ->
                        lastEncounter.value = last
                    }

                    // unroll all the events
                    val expandedList = mutableListOf<BLEContactEntity>()
                    list.forEach { bce ->
                        val ts = bce.timestamp
                        val btId = bce.btId
                        bce.enumeratedEvents.forEach { event ->
                            expandedList.add(
                                BLEContactEntity(
                                    btId = btId,
                                    timestamp = relativeTimestampToDate(ts, event.relativeTimestamp)
                                )
                            )
                        }
                    }

                    if(expandedList.isEmpty()) return@collect

                    val firstTs = expandedList.first().timestamp
                    val lastTs = expandedList.last().timestamp

                    // let's start counting from the straight hour before the first event
                    val startTs = Calendar.getInstance().apply {
                        time = firstTs
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    val outputList = mutableListOf<EncountersItem>()
                    while(startTs.timeInMillis < lastTs.time) {

                        val time = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(
                            startTs.time)
                        val count = expandedList.count {
                            it.timestamp.time >= startTs.time.time &&
                            it.timestamp.time < (startTs.time.time + 15 * 60 * 1000)
                        }
                        outputList.add(EncountersItem(time, count))
                        // add 15 minutes
                        startTs.add(Calendar.MINUTE, 15)
                    }
                    listModel.value = outputList
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
