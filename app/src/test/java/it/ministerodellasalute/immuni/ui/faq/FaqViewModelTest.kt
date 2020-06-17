package it.ministerodellasalute.immuni.ui.faq

import org.junit.Assert.*
import org.junit.Test

class FaqViewModelTest {

    @Test
    fun `fuzzy search ignores case`() {
        assertTrue("AbcDeFGhIJK".fuzzyContains("a"))
        assertTrue("AbcDeFGhIJK".fuzzyContains("bcDe"))
        assertTrue("AbcDeFGhIJK".fuzzyContains("k"))
        assertTrue("AbcDeFGhIJK".fuzzyContains("aK"))
        assertTrue("AbcDeFGhIJK".fuzzyContains("aEFgJk"))
        assertTrue("AbcDeFGhIJK".fuzzyContains("BcdH"))
    }

    @Test
    fun `fuzzy search simple scenarios`() {
        assertTrue("123456789".fuzzyContains("1"))
        assertTrue("123456789".fuzzyContains("12"))
        assertTrue("123456789".fuzzyContains("123"))
        assertTrue("123456789".fuzzyContains("9"))
        assertTrue("123456789".fuzzyContains("89"))
    }

    @Test
    fun `fuzzy search complex fuzzy search`() {
        assertTrue("123456789".fuzzyContains("79"))
        assertTrue("123456789".fuzzyContains("13"))
        assertTrue("123456789".fuzzyContains("24"))
        assertTrue("123456789".fuzzyContains("29"))
        assertTrue("123456789".fuzzyContains("19"))
        assertTrue("123456789".fuzzyContains("1389"))
        assertTrue("123456789".fuzzyContains("2357"))
    }

    @Test
    fun `fuzzy search should not find`() {
        assertFalse("123456789".fuzzyContains("9AB"))
        assertFalse("123456789".fuzzyContains("98"))
        assertFalse("123456789".fuzzyContains("21"))
        assertFalse("123456789".fuzzyContains("34790"))
        assertFalse("123456789".fuzzyContains("987654321"))
    }
}
