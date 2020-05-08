package org.immuni.android.debugmenu

import android.app.Application
import android.content.Intent
import org.immuni.android.debugmenu.overlay.DebugMenuGlobalTouchListener
import org.immuni.android.debugmenu.ui.DebugMenuActivity
import org.immuni.android.network.Network

/**
 * A debug menu used to show to the user some debugging features.
 *
 * This is the debug menu lib main class. It receives the user touch events
 * and decide if trigger or not a secret menu. The [DebugMenuTouchManager] is
 * responsible for this decision.
 *
 * @constructor create an instance of [DebugMenu] using its [DebugMenuConfiguration].
 * @param context
 * @param config the [DebugMenuConfiguration] injected by the app.
 * @param networking an instance of [Network]
 */
class DebugMenu(
    val context: Application,
    val config: DebugMenuConfiguration
) : DebugMenuTouchManagerListener {

    private val touchManager =
        DebugMenuTouchManager(this, config)
    private val globalTouchListener = DebugMenuGlobalTouchListener(context, touchManager)

    override fun onActivateSecretMenu() {
        val intent = Intent(context, DebugMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // This instance reference allows the UI
    // to refer to the SecretMenu object.

    init {
        instance = this
    }

    companion object {
        internal lateinit var instance: DebugMenu
    }
}
