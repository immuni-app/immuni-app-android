package com.bendingspoons.ascolto.ui.log

import androidx.lifecycle.*
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.ascolto.ui.log.model.FormModel
import com.bendingspoons.base.livedata.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinComponent
import java.io.Serializable

class LogViewModel(val handle: SavedStateHandle, private val database: AscoltoDatabase) : ViewModel(),
    KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var formModel = MediatorLiveData<FormModel>()
    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)

    private val _navigateToNextPage = MutableLiveData<Event<Boolean>>()
    val navigateToNextPage: LiveData<Event<Boolean>>
        get() = _navigateToNextPage

    private val _navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage: LiveData<Event<Boolean>>
        get() = _navigateToPrevPage

    init {
        // init
        if(handle.get<Serializable>(STATE_KEY) != null) {
            formModel.value = handle.get<Serializable>(STATE_KEY) as FormModel
        } else formModel.value = FormModel()

        formModel.addSource(savedStateLiveData) {
            formModel.value = it as? FormModel
        }
    }

    fun onNextTap() {
        _navigateToNextPage.value = Event(true)
    }

    fun onPrevTap() {
        _navigateToPrevPage.value = Event(true)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateFormModel(model: FormModel) {
        handle.set(STATE_KEY, model)
    }

    fun formModel(): FormModel? {
        return formModel.value
    }

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }
}