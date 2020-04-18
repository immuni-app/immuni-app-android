package com.bendingspoons.base.playstore

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object PlayStoreActions {

    fun goToPlayStoreAppDetails(context: Context) {
        val uri: Uri = Uri.parse("market://details?id=" + context.packageName)
        val goToPlayStore = Intent(Intent.ACTION_VIEW, uri)

        // To take into account Play Store's backstack, after pressing back button,
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
