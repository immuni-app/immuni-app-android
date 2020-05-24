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
