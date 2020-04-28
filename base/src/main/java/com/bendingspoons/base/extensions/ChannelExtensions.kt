package com.bendingspoons.base.extensions

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Receive all available elements up to [max].
 * Suspends for the first element if the channel is empty.
 */
internal suspend fun <E> ReceiveChannel<E>.receiveAvailable(max: Int): List<E> {
    if (max <= 0) {
        return emptyList()
    }

    val batch = mutableListOf<E>()
    if (this.isEmpty) {
        // suspend until the next message is ready
        batch.add(receive())
    }

    fun pollUntilMax() = if (batch.size >= max) null else poll()

    // consume all other messages that are ready
    var next = pollUntilMax()
    while (next != null) {
        batch.add(next)
        next = pollUntilMax()
    }

    return batch
}
