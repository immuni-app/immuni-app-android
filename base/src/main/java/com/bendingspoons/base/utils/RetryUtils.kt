package com.bendingspoons.base.utils

import kotlinx.coroutines.delay

// Retry with exponential backoff

suspend fun <T> retry(
        times: Int = Int.MAX_VALUE,
        initialDelay: Long = 1000,
        maxDelay: Long = 30*1000,
        factor: Double = 2.0,
        block: suspend () -> T,
        exitWhen: (T) -> Boolean,
        onIntermediateFailure: (T) -> Unit): T
{
    var currentDelay = initialDelay
    repeat(times) {
        val result = block()
        if(exitWhen(result)) return result
        else onIntermediateFailure(result)

        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}