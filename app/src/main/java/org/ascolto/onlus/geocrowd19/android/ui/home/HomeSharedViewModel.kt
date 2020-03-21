package org.ascolto.onlus.geocrowd19.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.util.isFlagSet
import org.ascolto.onlus.geocrowd19.android.util.setFlag
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
