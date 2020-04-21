package com.bendingspoons.secretmenu

import com.bendingspoons.concierge.ConciergeManager

interface SecretMenuConfiguration {

    val isDevelopmentDevice: () -> Boolean

    fun concierge(): ConciergeManager

    // available to anyone
    fun publicItems(): List<SecretMenuItem>

    // available only during debug
    fun debuggingItems(): List<SecretMenuItem>
}
