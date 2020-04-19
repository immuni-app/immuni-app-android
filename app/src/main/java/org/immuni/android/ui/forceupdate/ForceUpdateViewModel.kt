package org.immuni.android.ui.forceupdate

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import com.bendingspoons.base.playstore.PlayStoreActions
import com.bendingspoons.base.utils.ExternalLinksHelper
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import org.immuni.android.R
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.koin.core.KoinComponent
import org.koin.core.inject


class ForceUpdateViewModel : ViewModel(), KoinComponent {

    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val concierge: ConciergeManager by inject()

    fun goToPlayStoreAppDetails(context: Context) {

        // if we have a custon url
        oracle.settings()?.appUpdateUrl?.let { url->
            startDownload(context, url)
            return
        }

        // otherwise go to play store
        PlayStoreActions.goToPlayStoreAppDetails(context)
    }

    private fun startDownload(context: Context, url: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = Uri.parse(url)
        val request = DownloadManager.Request(uri).apply {
            setTitle(context.getString(R.string.immuni_update_file_title))
            setDescription(context.getString(R.string.immuni_update_file_description))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            addRequestHeader("device-id", concierge.backupPersistentId.id)
        }

        downloadManager.enqueue(request)
    }
}
