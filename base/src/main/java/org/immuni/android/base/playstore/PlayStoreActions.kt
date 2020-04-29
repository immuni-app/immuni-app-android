package org.immuni.android.base.playstore

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Play Store utilities.
 */
object PlayStoreActions {

    /**
     * Open the Google Play app and navigate to the app page.
     * If the Google Play app is not available, it uses the browser.
     */
    fun goToPlayStoreAppDetails(context: Context) {
        val uri: Uri = Uri.parse("market://details?id=" + context.packageName)
        val goToPlayStore = Intent(Intent.ACTION_VIEW, uri)

        // To take into account Play Store's back stack, after pressing back button,
        // to be taken back to our application, we need to add following flags to the intent
        goToPlayStore.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            context.startActivity(goToPlayStore)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
