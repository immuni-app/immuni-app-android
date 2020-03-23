package org.ascolto.onlus.geocrowd19.android

import android.app.Application
import android.content.Context
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import androidx.lifecycle.ProcessLifecycleOwner
import com.bendingspoons.base.lifecycle.AppLifecycleEvent
import com.bendingspoons.base.lifecycle.AppLifecycleObserver
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.theirs.Theirs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AscoltoApplication : Application() {

    private lateinit var concierge: ConciergeManager
    private lateinit var oracle: Oracle<AscoltoSettings, AscoltoMe>
    private lateinit var pico: Pico
    private lateinit var theirs: Theirs


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
            androidContext(this@AscoltoApplication)
            modules(appModule)
        }

        concierge = get()
        oracle = get()
        pico = get()
        theirs = get()

        pico.setup()

        initDB()
    }

    private fun initDB() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // TODO
            }
        }
    }

    companion object {
        lateinit var appContext: Context
        val isForeground = ConflatedBroadcastChannel(false)
    }
}
