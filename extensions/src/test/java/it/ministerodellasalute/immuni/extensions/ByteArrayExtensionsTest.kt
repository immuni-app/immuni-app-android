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

import it.ministerodellasalute.immuni.extensions.utils.toHex
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
