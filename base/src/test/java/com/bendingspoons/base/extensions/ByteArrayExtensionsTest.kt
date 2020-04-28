package com.bendingspoons.base.extensions

import junit.framework.Assert.assertEquals
import org.junit.Test

class ByteArrayExtensionsTest {

    @Test
    fun `test byte array to hex`() {
        val array = ByteArray(5).apply {
            this[0] = 123
            this[1] = 45
            this[2] = 6
            this[3] = 88
            this[4] = 1
        }
        assertEquals("7B2D065801", array.toHex())
    }

    @Test
    fun `test empty byte array to hex`() {
        val array = ByteArray(0)
        assertEquals("", array.toHex())
    }

    @Test
    fun `test one byte array to hex`() {
        val array = ByteArray(1).apply {
            this[0] = 0
        }
        assertEquals("00", array.toHex())

        array.apply {
            this[0] = 1
        }
        assertEquals("01", array.toHex())

        array.apply {
            this[0] = 15
        }
        assertEquals("0F", array.toHex())
    }
}