package org.immuni.android.analytics.userconsent

import android.content.Context
import org.immuni.android.base.utils.fromJson
import org.immuni.android.base.utils.toJson
import org.immuni.android.analytics.PicoConfiguration
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserConsentTest {

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    internal lateinit var store: UserConsentStore

    @MockK(relaxed = true)
    lateinit var config: PicoConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `user consent level is serialized and deserialized correctly`() {
        val levels = listOf(
            UserConsentLevel.ACCEPTED,
            UserConsentLevel.DENIED,
            UserConsentLevel.UNKNOWN
        )
        levels.forEach { level ->
            val json = toJson(level)
            val deserialized = fromJson<UserConsentLevel>(json)
            assertEquals(level, deserialized)
        }
    }

    @Test
    fun `when accessing level the store is invoked properly`() {
        val userConsent = UserConsent(context, config, store)

        val level = userConsent.level

        verify(exactly = 1) { store.load(any()) }
        verify(exactly = 0) { store.save(any(), any()) }
    }

    @Test
    fun `when setting level the store is invoked properly`() {
        val userConsent = UserConsent(context, config, store)

        userConsent.level = UserConsentLevel.DENIED

        verify(exactly = 0) { store.load(any()) }
        verify(exactly = 1) { store.save(any(), UserConsentLevel.DENIED) }
    }
}