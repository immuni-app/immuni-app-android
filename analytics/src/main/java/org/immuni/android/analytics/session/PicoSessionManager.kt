package org.immuni.android.analytics.session

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.extensions.lifecycle.AppLifecycleEvent.*
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.analytics.PicoEventManager
import org.immuni.android.analytics.PicoFlow
import org.immuni.android.analytics.model.Session
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timer

internal class PicoSessionManager(
    context: Context,
    private val eventManager: CompletableDeferred<PicoEventManager>,
    private val picoFlow: PicoFlow
) {
    companion object {
        const val lastSessionKey = "Pico-Last-Session"
        const val sessionCrashedKey = "Pico-Crashed"
    }
    private var isInBackground = true
    private val storage = KVStorage("PICO_SESSION_MANAGER", context, encrypted = false)

    private var currentSession: PicoSession
        get() {
            var session = storage.load<PicoSession>(lastSessionKey)
            if (session == null) {
                session = PicoSession()
            }
            return session
        }
        set(value) {
            storage.save(lastSessionKey, value)
        }

    private var timer: Timer
    private val lifecycleObserver: AppLifecycleObserver

    private var mDefaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    private val mCaughtExceptionHandler =
        Thread.UncaughtExceptionHandler { thread, ex ->
            // Custom logic goes here
            storage.save(sessionCrashedKey, true)
            // this will make Firebase Crashlytics do its job
            mDefaultUncaughtExceptionHandler?.uncaughtException(thread, ex)
        }

    init {
        timer = startTimer()

        checkCrashedSession()

        lifecycleObserver = AppLifecycleObserver()

        GlobalScope.launch {
            lifecycleObserver.consumeEach {
                when (it) {
                    ON_START -> {
                        isInBackground = false
                        trackStartSession()
                    }
                    ON_STOP -> {
                        isInBackground = true
                        trackEndSession()
                        picoFlow.flush()
                    }
                    else -> {
                    }
                }
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // cache a reference to default uncaught exception handler
        mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        // set custom UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(mCaughtExceptionHandler)
    }

    private fun checkCrashedSession() {
        val lastSessionEndData = lastSessionEndData()
        if (lastSessionEndData != null) {
            GlobalScope.launch {
                // maybe a real crash or an app kill (swipe up or force kill)

                val crashed = storage.load<Boolean>(sessionCrashedKey, false)
                storage.delete(sessionCrashedKey)

                currentSession = currentSession.copy(isCrashed = crashed)
                trackEndSession()
            }
        }
    }

    private fun startTimer(): Timer {
        return timer(initialDelay = 1000, period = 1000) {
            currentSession = currentSession.copy(
                lastDate = Date(),
                durationMillis = Date().time - currentSession.startDate.time
            )
        }
    }

    private fun lastSessionEndData(): PicoSessionEndData? {
        val lastSession = storage.load<PicoSession>(lastSessionKey) ?: return null

        return PicoSessionEndData(
            sessionStartId = lastSession.id,
            crashed = lastSession.isCrashed,
            duration = lastSession.durationMillis / 1000.0,
            sessionData = lastSession.sessionData
        )
    }

    private fun currentSessionStartData() = PicoSessionStartData(id = currentSession.id)

    private suspend fun sendSessionEnd(sessionEndData: PicoSessionEndData) {
        eventManager.await().trackEvent(Session(sessionData = sessionEndData))
        storage.delete(lastSessionKey)
    }

    private suspend fun sendSessionStart(sessionStartData: PicoSessionStartData) {
        eventManager.await().trackEvent(Session(id = sessionStartData.id, sessionData = sessionStartData))
    }

    private suspend fun trackEndSession() {
        timer.cancel()

        val lastSessionEndData = lastSessionEndData()
        if (lastSessionEndData != null) {
            sendSessionEnd(lastSessionEndData)
        }
    }

    private suspend fun trackStartSession() {
        currentSession = PicoSession()
        sendSessionStart(currentSessionStartData())

        timer = startTimer()
    }

    fun getSecondsFromSessionStart(): Double? {
        return when(isInBackground()) {
            true -> null
            false -> (System.currentTimeMillis()/1000.0 - currentSession.startDate.time/1000.0)
        }
    }

    fun getSessionId(): String? {
        return when(isInBackground()) {
            true -> null
            false -> currentSession.id
        }
    }

    fun isInBackground(): Boolean {
        return isInBackground
    }

    fun getLastForegroundSessionId(): String? {
        return when(isInBackground()) {
            true -> lastSessionEndData()?.sessionStartId
            false -> null
        }
    }
}
