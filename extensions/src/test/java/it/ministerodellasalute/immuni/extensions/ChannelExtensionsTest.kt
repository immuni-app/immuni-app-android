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

package it.ministerodellasalute.immuni.extensions

import it.ministerodellasalute.immuni.extensions.utils.receiveAvailable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@ExperimentalCoroutinesApi
class ChannelExtensionsTest {

    @Test
    fun `test broadcast channel receive available`() = runBlocking {
        val channel = Channel<Int>(capacity = Channel.UNLIMITED)

        for (i in 0 until 13) {
            channel.send(i)
        }

        var elements = channel.receiveAvailable(5)
        assertEquals(5, elements.size)
        elements = channel.receiveAvailable(5)
        assertEquals(5, elements.size)
        elements = channel.receiveAvailable(5)
        assertEquals(3, elements.size)
        val emptyList = withTimeoutOrNull(100) {
            channel.receiveAvailable(5)
        }
        assertNull(emptyList)
    }
}
