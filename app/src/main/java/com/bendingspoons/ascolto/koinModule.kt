package com.bendingspoons.ascolto

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.ascolto.ui.log.LogViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // single instance of TDFDatabase
    single {
        Room.databaseBuilder(
            androidContext(),
            AscoltoDatabase::class.java,
            "ascolto_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    viewModel { (handle: SavedStateHandle) -> LogViewModel(handle, get()) }
}
