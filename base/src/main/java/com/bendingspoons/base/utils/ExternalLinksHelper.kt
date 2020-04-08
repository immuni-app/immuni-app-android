package com.bendingspoons.base.utils

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
}