package org.immuni.android.managers

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.immuni.android.ImmuniApplication
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.data.SettingsDataSource
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.ui.forceupdate.ForceUpdateActivity
import org.immuni.android.util.log

/**
 * Listens to changes in [ImmuniSettings] Flow and trigger a force update UI.
 */
class ForceUpdateManager(
    val context: Context,
    val settingsDataSource: SettingsDataSource,
    val lifecycleObserver: AppLifecycleObserver
) {
    init {
        registerToMinBuildVersionChanges()
    }

    private fun registerToMinBuildVersionChanges() {
        GlobalScope.launch {
            settingsDataSource.settingsFlow().collect { settings ->
                if (settings.minBuildVersion > DeviceUtils.appVersionCode(context)) {
                    withContext(Dispatchers.Main) {
                        showForceUpdate(settings.minBuildVersion)
                    }
                }
            }
        }
    }

    private fun showForceUpdate(minVersionCode: Int) {
        log("ForceUpdate! Min version is $minVersionCode")
        // avoid to open the activity while the app is in background
        if (lifecycleObserver.isInForeground &&
            !ForceUpdateActivity.isOpen) {
            val context =
                ImmuniApplication.appContext
            val intent = Intent(context, ForceUpdateActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(intent)
        }
    }
}
