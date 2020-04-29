package org.immuni.android.secretmenu

import org.immuni.android.ids.ConciergeManager

/**
 * This is the secret menu configuration
 * the app injects into the [SecretMenu].
 */
interface SecretMenuConfiguration {

    val isDevelopmentDevice: () -> Boolean

    fun concierge(): ConciergeManager

    // available to anyone
    fun publicItems(): List<SecretMenuItem>

    // available only during debug
    fun debuggingItems(): List<SecretMenuItem>
}
