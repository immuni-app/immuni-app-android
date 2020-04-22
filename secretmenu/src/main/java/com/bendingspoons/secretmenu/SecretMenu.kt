package com.bendingspoons.secretmenu

import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.oracle.api.model.OracleMe
import com.bendingspoons.oracle.api.model.OracleSettings
import com.bendingspoons.secretmenu.ui.SecretMenuActivity

/**
 * A secret menu used to show to the user some debugging features.
 *
 * This is the secret menu lib main class. It receives the user touch events
 * and decide if trigger or not a secret menu. The [SecretMenuTouchManager] is
 * responsible for this decision.
 *
 * @constructor create an instance of [SecretMenu] using its [SecretMenuConfiguration].
 * @param context
 * @param config the [SecretMenuConfiguration]
 * @param oracle an instance of [Oracle]
 */
class SecretMenu(
    val context: Context,
    val config: SecretMenuConfiguration,
    val oracle: Oracle<OracleSettings, OracleMe>
) : SecretMenuTouchManagerListener {

    private val touchManager = SecretMenuTouchManager(this, config)

    override fun onActivateSecretMenu() {
        val intent = Intent(context, SecretMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun onTouchEvent(ev: MotionEvent) {
        touchManager.onTouchEvent(ev)
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
