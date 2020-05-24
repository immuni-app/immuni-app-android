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

import it.ministerodellasalute.immuni.extensions.utils.round
import junit.framework.Assert.assertEquals
import org.junit.Test

class DoubleExtensionsTest {

    @Test
    fun `test round double decimals`() {
        assertEquals(43.3, 43.32323.round(1))
        assertEquals(43.33, 43.3267.round(2))
        assertEquals(0.5, 0.5455.round(1))
        assertEquals(0.0, 0.4455.round(0))
        assertEquals(5.0, 4.6.round(0))
    }
}
