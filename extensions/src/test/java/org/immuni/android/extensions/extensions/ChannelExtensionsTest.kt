package org.immuni.android.extensions.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.immuni.android.extensions.utils.receiveAvailable
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
