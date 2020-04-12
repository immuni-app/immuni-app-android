package com.bendingspoons.pico.experiments

import android.content.Context
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.pico.PicoEventManager
import com.bendingspoons.pico.model.ExperimentSegmentsReceived
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

internal class ExperimentsSegmentReceivedManager(
    private val store: ExperimentsStore,
    private val settingsFlow: Flow<OracleSettings>,
    private val eventManager: PicoEventManager
) {

    suspend fun start() {
        settingsFlow
            .map { it.experimentsSegments }
            .collect { new ->
                val old = store.loadSegments()
                if (new != old) {
                    sendExperimentsEvent()
                }
                store.storeSegments(new)
            }
    }

    private suspend fun sendExperimentsEvent() {
        eventManager.trackEvent(ExperimentSegmentsReceived())
    }
}

class ExperimentsStore(context: Context, encrypted: Boolean) {
    companion object {
        const val NAME = "PICO_EXPERIMENTS_MANAGER"
        const val EXPERIMENTS_KEY = "experiments"
    }

    private val store = KVStorage(NAME, context, encrypted = encrypted)

    fun storeSegments(experiments: Map<String, Int>) {
        store.save(EXPERIMENTS_KEY, experiments)
    }

    fun loadSegments(): Map<String, Int>? {
        return store.load(EXPERIMENTS_KEY)
    }
}
