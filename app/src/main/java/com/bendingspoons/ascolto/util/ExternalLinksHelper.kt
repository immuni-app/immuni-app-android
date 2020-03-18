package com.bendingspoons.ascolto.util

import android.content.Intent
import android.net.Uri
import com.bendingspoons.ascolto.AscoltoApplication
import com.bendingspoons.ascolto.R
import com.bendingspoons.ascolto.toast

object ExternalLinksHelper {

    /**
     * Open the given url with a web browser app.
     * @param url
     */
    fun openLink(url: String) {
        val context = AscoltoApplication.appContext
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val manager = context.packageManager
        val infos = manager.queryIntentActivities(browserIntent, 0)
        if (infos.size > 0) {
            context.startActivity(browserIntent)
        } else {
            toast(R.string.no_app_can_open_the_link)
        }
    }
}