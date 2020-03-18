package com.bendingspoons.ascolto

import android.app.Application
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AscoltoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@AscoltoApplication)
            modules(appModule)
        }

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
