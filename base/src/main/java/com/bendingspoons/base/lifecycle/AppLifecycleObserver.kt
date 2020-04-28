package com.bendingspoons.base.lifecycle

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectClause2

/**
 * An application [LifecycleObserver] that implements
 */
@ExperimentalCoroutinesApi
class AppLifecycleObserver(private val coroutineScope: CoroutineScope = GlobalScope) :
    LifecycleObserver, BroadcastChannel<AppLifecycleEvent> {

    var isInForeground: Boolean = false
        private set
    val isInBackground: Boolean
        get() = !isInForeground

    var isPaused: Boolean = false
        private set
    val isActive: Boolean
        get() = !isPaused

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onForeground() {
        isInForeground = true
        coroutineScope.launch {
            send(AppLifecycleEvent.ON_START)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onBackground() {
        isInForeground = false
        coroutineScope.launch {
            send(AppLifecycleEvent.ON_STOP)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        isPaused = true
        coroutineScope.launch {
            send(AppLifecycleEvent.ON_PAUSE)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        isPaused = false
        coroutineScope.launch {
            send(AppLifecycleEvent.ON_RESUME)
        }
    }

    private val channel = ConflatedBroadcastChannel<AppLifecycleEvent>()

    // BroadcastChannel methods

    override val isClosedForSend: Boolean
        get() = channel.isClosedForSend

    override val isFull: Boolean
        get() = false

    override val onSend: SelectClause2<AppLifecycleEvent, SendChannel<AppLifecycleEvent>>
        get() = channel.onSend

    override fun cancel(cause: Throwable?): Boolean {
        when (cause) {
            null -> channel.cancel(null)
            is CancellationException -> channel.cancel(cause)
            else -> return false
        }
        return true
    }

    override fun cancel(cause: CancellationException?) {
        return channel.cancel(cause)
    }

    override fun close(cause: Throwable?): Boolean {
        return channel.close(cause)
    }

    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        return channel.invokeOnClose(handler)
    }

    override fun offer(element: AppLifecycleEvent): Boolean {
        return channel.offer(element)
    }

    override fun openSubscription(): ReceiveChannel<AppLifecycleEvent> {
        return channel.openSubscription()
    }

    override suspend fun send(element: AppLifecycleEvent) {
        channel.send(element)
    }
}

enum class AppLifecycleEvent {
    ON_START, ON_STOP, ON_PAUSE, ON_RESUME
}
