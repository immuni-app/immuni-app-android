package org.immuni.android.managers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.api.oracle.model.BtId
import org.immuni.android.api.oracle.model.BtIds
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class BtIdsManager(val context: Context) : KoinComponent {
    private val apiManager: ApiManager by inject()
    private var btIds: BtIds? = null
    private var timeCorrection = 0L
    private val LOG_TAG = "BtIdsManager"
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
        var hadSucces = false
        while (!hadSucces) {
            try {
                val response = apiManager.getBtIds()
                if (response.isSuccessful) {
                    btIds = response.body()
                    timeCorrection = Date().time - (btIds!!.serverTimestamp.toLong() * 1000L)
                    hadSucces = true
                }
            } catch (e: Exception) {
                Log.d(LOG_TAG, "### error fetching ids, trying againg")
            }
            if (!hadSucces) {
                delay(5 * 1000)
            }
        }

        // schedule a new fetch in 30 minutes (non blocking)
        GlobalScope.launch {
            delay(30 * 60 * 1000)
            refresh()
        }
    }
}
