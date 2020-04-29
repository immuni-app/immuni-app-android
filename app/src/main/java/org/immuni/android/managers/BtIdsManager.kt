package org.immuni.android.managers

import android.content.Context
import org.immuni.android.extensions.utils.retry
import kotlinx.coroutines.*
import org.immuni.android.api.ImmuniAPIRepository
import org.immuni.android.api.model.BtId
import org.immuni.android.api.model.BtIds
import org.immuni.android.networking.api.NetworkResource
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class BtIdsManager(val context: Context) : KoinComponent {
    private val immuniAPIRepository: ImmuniAPIRepository by inject()
    private var btIds: BtIds? = null
    private var timeCorrection = 0L

    fun correctTime() : Long {
        return Date().time + timeCorrection
    }

    fun getCurrentBtId(): BtId? {
        return btIds?.ids?.firstOrNull()
    }

    fun isNotExpired(btId: BtId) : Boolean {
        return correctTime() < (btId.expirationTimestamp * 1000.0).toLong()
    }

    suspend fun getOrFetchActiveBtId(): BtId {
        val activeBtId = btIds?.ids?.firstOrNull { isNotExpired(it) }
        if (activeBtId == null) {
            refresh()
            return getOrFetchActiveBtId()
        }

        return activeBtId
    }

    suspend fun setup() {
        refresh()
    }

    private suspend fun refresh() {
        val block = suspend {
            val response = immuniAPIRepository.getBtIds()
            val result = runCatching {
                when(response) {
                    is NetworkResource.Success -> {
                        btIds = response.data
                        timeCorrection = Date().time - (btIds!!.serverTimestamp.toLong() * 1000L)
                    }
                    else -> {
                        // nothing to do
                    }
                }
                response is NetworkResource.Success
            }
            result.getOrNull() ?: false
        }

        // exponential backoff
        retry(times = Int.MAX_VALUE,
            initialDelay = 3000,
            maxDelay = 120000,
            block = block,
            exitWhen = {success -> success },
            onIntermediateFailure = {})
    }

    var isRefreshScheduled = false
    suspend fun scheduleRefresh() {
        if(isRefreshScheduled) return
        isRefreshScheduled = true

        delay(30 * 60 * 1000)
        isRefreshScheduled = false
        refresh()
    }
}
