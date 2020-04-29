package org.immuni.android

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ProcessLifecycleOwner
import org.immuni.android.base.lifecycle.AppLifecycleObserver
import org.immuni.android.ids.IdsManager
import org.immuni.android.networking.Networking
import org.immuni.android.analytics.Pico
import org.immuni.android.fcm.FirebaseFCM
import org.immuni.android.secretmenu.SecretMenu
import org.immuni.android.networking.model.ImmuniMe
import org.immuni.android.networking.model.ImmuniSettings
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.receivers.RestarterReceiver
import org.immuni.android.receivers.ShutdownReceiver
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ImmuniApplication : Application() {

    private lateinit var ids: IdsManager
    private lateinit var networking: Networking<ImmuniSettings, ImmuniMe>
    private lateinit var pico: Pico
    private lateinit var fcm: FirebaseFCM
    private lateinit var secretMenu: SecretMenu
    private lateinit var surveyNotificationManager: SurveyNotificationManager

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // register lifecycle observer
        lifecycleObserver = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // start Koin DI module
        startKoin {
            androidLogger()
            androidContext(this@ImmuniApplication)
            modules(appModule)
        }

        ids = get()
        networking = get()
        pico = get()
        fcm = get()
        secretMenu = get()
        surveyNotificationManager = get()

        pico.setup()

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
        lateinit var lifecycleObserver: AppLifecycleObserver
    }
}
