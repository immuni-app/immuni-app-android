package org.immuni.android.secretmenu

import android.app.Application
import android.content.Intent
import org.immuni.android.networking.Networking
import org.immuni.android.networking.api.model.NetworkingMe
import org.immuni.android.networking.api.model.NetworkingSettings
import org.immuni.android.secretmenu.overlay.SecretMenuGlobalTouchListener
import org.immuni.android.secretmenu.ui.SecretMenuActivity

/**
 * A secret menu used to show to the user some debugging features.
 *
 * This is the secret menu lib main class. It receives the user touch events
 * and decide if trigger or not a secret menu. The [SecretMenuTouchManager] is
 * responsible for this decision.
 *
 * @constructor create an instance of [SecretMenu] using its [SecretMenuConfiguration].
 * @param context
 * @param config the [SecretMenuConfiguration] injected by the app.
 * @param networking an instance of [Networking]
 */
class SecretMenu(
    val context: Application,
    val config: SecretMenuConfiguration,
    val networking: Networking<NetworkingSettings, NetworkingMe>
) : SecretMenuTouchManagerListener {

    private val touchManager =
        SecretMenuTouchManager(this, config)
    private val globalTouchListener = SecretMenuGlobalTouchListener(context, touchManager)

    override fun onActivateSecretMenu() {
        val intent = Intent(context, SecretMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // This instance reference allows the UI
    // to refer to the SecretMenu object.

    init {
        instance = this
    }

    companion object {
        internal lateinit var instance: SecretMenu
    }
}
