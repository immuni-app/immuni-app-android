/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.utils

import kotlinx.coroutines.delay

/**
 * Retry with exponential backoff.
 *
 * Try [times] times to execute the [block] until the [exitWhen] block is satisfied using
 * exponential back off.
 * When fails invoke the [onIntermediateFailure] block.
 *
 * @param times max times of attempt.
 * @param initialDelay the first initial delay after the first attempt.
 * @param maxDelay the maximum delay between subsequent retries.
 * @param factor the exponential back off factor.
 * @param block the desired action.
 * @param exitWhen the exit block that indicates when we can return.
 * @param onIntermediateFailure a block to invoke on intermediate failures.
 */
suspend fun <T> retry(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 1000,
    maxDelay: Long = 30 * 1000,
    factor: Double = 2.0,
    block: suspend () -> T,
    exitWhen: (T) -> Boolean,
    onIntermediateFailure: (T) -> Unit
): T {
    var currentDelay = initialDelay
    repeat(times) {
        val result = block()
        if (exitWhen(result)) return result
        else onIntermediateFailure(result)

        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}
