package org.immuni.android

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ProcessLifecycleOwner
import org.immuni.android.data.SettingsDataSource
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.network.Network
import org.immuni.android.fcm.FirebaseFCM
import org.immuni.android.debugmenu.DebugMenu
import org.immuni.android.managers.ForceUpdateManager
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.receivers.RestarterReceiver
import org.immuni.android.receivers.ShutdownReceiver
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ImmuniApplication : Application() {

    private lateinit var network: Network
    private lateinit var fcm: FirebaseFCM
    private lateinit var settingsDataSource: SettingsDataSource
    private lateinit var forceUpdateManager: ForceUpdateManager
    private lateinit var debugMenu: DebugMenu
    private lateinit var surveyNotificationManager: SurveyNotificationManager
    private lateinit var lifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // start Koin DI module
        startKoin {
            androidLogger()
            androidContext(this@ImmuniApplication)
            modules(appModule)
        }

        network = get()
        fcm = get()
        debugMenu = get()
        surveyNotificationManager = get()
        settingsDataSource = get()
        forceUpdateManager = get()

        // register lifecycle observer
        lifecycleObserver = get()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        startWorkers()
        registerReceivers()
    }

    private fun startWorkers() {
        val alarmIntent = Intent(appContext, RestarterReceiver::class.java)
        applicationContext.sendBroadcast(alarmIntent)
    }

    private fun registerReceivers() {
        val filter = IntentFilter(Intent.ACTION_SHUTDOWN)
        val mReceiver: BroadcastReceiver = ShutdownReceiver()
        registerReceiver(mReceiver, filter)
    }

    companion object {
        lateinit var appContext: Context
    }
}
