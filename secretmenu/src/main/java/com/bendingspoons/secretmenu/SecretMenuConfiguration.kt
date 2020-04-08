package com.bendingspoons.secretmenu

import com.bendingspoons.concierge.ConciergeManager

interface SecretMenuConfiguration {

    val isDevelopmentDevice: () -> Boolean

    fun concierge(): ConciergeManager

    // available to anyone, without password (use these for support kind of tasks)
    fun publicItems(): List<SecretMenuItem>

    // available only on spooner devices
    fun spoonerItems(): List<SecretMenuItem>
}
