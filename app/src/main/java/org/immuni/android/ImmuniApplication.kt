package org.immuni.android

import android.app.Application
import android.content.Context
import android.content.Intent
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import androidx.lifecycle.ProcessLifecycleOwner
import com.bendingspoons.base.lifecycle.AppLifecycleEvent
import com.bendingspoons.base.lifecycle.AppLifecycleObserver
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.theirs.Theirs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.service.RestarterReceiver
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ImmuniApplication : Application() {

    private lateinit var concierge: ConciergeManager
    private lateinit var oracle: Oracle<ImmuniSettings, ImmuniMe>
    private lateinit var pico: Pico
    private lateinit var theirs: Theirs
    private lateinit var surveyNotificationManager: SurveyNotificationManager

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val lifecycleObserver = AppLifecycleObserver()
        GlobalScope.launch {
            lifecycleObserver.consumeEach { event ->
                when (event) {
                    AppLifecycleEvent.ON_START -> {
                        isForeground.send(true)
                    }
                    AppLifecycleEvent.ON_STOP -> {
                        isForeground.send(false)
                    }
                    else -> {
                    }
                }
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@ImmuniApplication)
            modules(appModule)
        }

        concierge = get()
        oracle = get()
        pico = get()
        theirs = get()
        surveyNotificationManager = get()

        pico.setup()

        startWorkers()
    }

    private fun startWorkers() {
        val alarmIntent = Intent(appContext, RestarterReceiver::class.java)
        applicationContext.sendBroadcast(alarmIntent)
    }

    companion object {
        lateinit var appContext: Context
        val isForeground = ConflatedBroadcastChannel(false)
    }
}
