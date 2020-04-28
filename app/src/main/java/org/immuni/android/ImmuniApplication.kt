package org.immuni.android

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ProcessLifecycleOwner
import com.bendingspoons.base.lifecycle.AppLifecycleEvent
import com.bendingspoons.base.lifecycle.AppLifecycleObserver
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.theirs.Theirs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.receivers.RestarterReceiver
import org.immuni.android.receivers.ShutdownReceiver
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ImmuniApplication : Application() {

    private lateinit var concierge: ConciergeManager
    private lateinit var oracle: Oracle<ImmuniSettings, ImmuniMe>
    private lateinit var pico: Pico
    private lateinit var theirs: Theirs
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

        concierge = get()
        oracle = get()
        pico = get()
        theirs = get()
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
