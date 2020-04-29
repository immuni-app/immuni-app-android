package org.immuni.android.secretmenu

/**
 * Listens to [SecretMenuTouchManager] events.
 */
interface SecretMenuTouchManagerListener {
    /**
     * Invoked when the [SecretMenuTouchManager] decides the secret menu must be opened.
     */
    fun onActivateSecretMenu()
}