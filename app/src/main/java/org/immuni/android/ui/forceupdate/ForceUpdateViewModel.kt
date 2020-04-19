package org.immuni.android.ui.forceupdate

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.playstore.PlayStoreActions
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.toast
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.lang.Exception

class ForceUpdateViewModel : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    val concierge: ConciergeManager by inject()

    val loading = MutableLiveData<Boolean>()
    val downloading = MutableLiveData<Boolean>()

    fun goToPlayStoreAppDetails(fragment: Fragment) {

        val activity = fragment.requireActivity()

        // if we have a custon url
        oracle.settings()?.appUpdateUrl?.let { url->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.applicationContext.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.applicationContext.packageName))
                fragment.startActivityForResult(intent, 20999)
                return
            }

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    openAppSettings(activity)
                } else {
                    // No explanation needed, we can request the permission.
                    fragment.requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        20999)

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
                startDownload(activity.applicationContext, url)
                downloading.value = true
            }

            return
        }

        // otherwise go to play store
        PlayStoreActions.goToPlayStoreAppDetails(activity.applicationContext)
    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + ImmuniApplication.appContext.packageName)
        activity.startActivity(intent)
    }

    private fun startDownload(context: Context, url: String) {

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = Uri.parse(url)

        val request = DownloadManager.Request(uri).apply {
            setTitle("immuni.apk")
            setDescription(context.getString(R.string.immuni_update_file_description))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "immuni.apk")
            addRequestHeader("device-id", concierge.backupPersistentId.id)
        }

        val downloadId = downloadManager.enqueue(request)

        /*
        //set BroadcastReceiver to install app when .apk is downloaded
        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val install = Intent(Intent.ACTION_VIEW)
                install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                install.setDataAndType(
                    Uri.fromFile(File(Environment.getDownloadCacheDirectory(), "immuni.apk")),
                    downloadManager.getMimeTypeForDownloadedFile(downloadId)
                )
                context.startActivity(install)
                context.unregisterReceiver(this)
            }
        }
        //register receiver for when .apk download is compete
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

         */
    }
}
