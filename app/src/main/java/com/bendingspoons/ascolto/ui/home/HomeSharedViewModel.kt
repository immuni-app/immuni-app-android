package com.bendingspoons.ascolto.ui.home

import androidx.lifecycle.ViewModel
import com.bendingspoons.ascolto.db.AscoltoDatabase
import kotlinx.coroutines.*
import org.koin.core.KoinComponent

class HomeSharedViewModel(val database: AscoltoDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
