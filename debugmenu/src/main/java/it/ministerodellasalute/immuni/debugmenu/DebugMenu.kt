/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.debugmenu

import android.app.Application
import android.content.Intent
import it.ministerodellasalute.immuni.debugmenu.overlay.DebugMenuGlobalTouchListener
import it.ministerodellasalute.immuni.debugmenu.ui.DebugMenuActivity
import it.ministerodellasalute.immuni.network.Network

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
