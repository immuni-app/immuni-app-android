package org.immuni.android.extensions.extensions

import junit.framework.Assert.assertEquals
import org.immuni.android.extensions.utils.round
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