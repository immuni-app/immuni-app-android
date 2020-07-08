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

package it.ministerodellasalute.immuni.extensions.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ExternalLinksHelper {

    /**
     * Open the given url with a web browser app.
     * @param context
     * @param url
     * @param errorMessage, to be shown if it impossible to open the link with any app.
     */
    fun openLink(context: Context, url: String, errorMessage: String = "Cannot open the link.") {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val manager = context.packageManager
        val infos = manager.queryIntentActivities(browserIntent, 0)
        if (infos.size > 0) {
            context.startActivity(browserIntent)
        } else {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, message: String, subject: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
