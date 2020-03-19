package com.bendingspoons.ascolto

import android.app.Application
import android.content.Context
import com.bendingspoons.ascolto.api.oracle.model.AscoltoMe
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.concierge.ConciergeManager
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.theirs.Theirs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    }
}
