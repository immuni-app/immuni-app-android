package com.bendingspoons.ascolto.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.ascolto.util.isFlagSet
import com.bendingspoons.ascolto.util.setFlag
import com.bendingspoons.base.livedata.Event
import kotlinx.coroutines.*
import org.koin.core.KoinComponent

class HomeSharedViewModel(val database: AscoltoDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _showAddFamilyMemberDialog = MutableLiveData<Event<Boolean>>()
    val showAddFamilyMemberDialog: LiveData<Event<Boolean>>
        get() = _showAddFamilyMemberDialog

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onHomeResumed() {
        uiScope.launch {
            delay(500)
            if(!isFlagSet("family_add_member_popup_showed")) {
                _showAddFamilyMemberDialog.value = Event(true)
                setFlag("family_add_member_popup_showed", true)
            }
        }
    }
}
