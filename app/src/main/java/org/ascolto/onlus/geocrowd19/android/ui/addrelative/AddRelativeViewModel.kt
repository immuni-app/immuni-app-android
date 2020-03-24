package org.ascolto.onlus.geocrowd19.android.ui.addrelative

import androidx.lifecycle.*
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.models.Nickname
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.addrelative.fragment.profile.RelativeContentFragment
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class AddRelativeViewModel(val handle: SavedStateHandle, private val database: AscoltoDatabase) :
    ViewModel(), KoinComponent {

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val pico: Pico by inject()
    private val apiManager: ApiManager by inject()

    var partialUserInfo = MediatorLiveData<RelativeInfo>()

    private var savedStateLiveData = handle.getLiveData<Serializable>(STATE_KEY)

    private val _navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToMainPage: LiveData<Event<Boolean>>
        get() = _navigateToMainPage

    private val _navigateToNextPage = MutableLiveData<Event<Class<out RelativeContentFragment>>>()
    val navigateToNextPage: LiveData<Event<Class<out RelativeContentFragment>>>
        get() = _navigateToNextPage

    private val _navigateToPrevPage = MutableLiveData<Event<Boolean>>()
    val navigateToPrevPage: LiveData<Event<Boolean>>
        get() = _navigateToPrevPage

    val loading = MutableLiveData<Boolean>()

    init {
        // init
        if (handle.get<Serializable>(STATE_KEY) != null) {
            partialUserInfo.value = handle.get<Serializable>(STATE_KEY) as RelativeInfo
        } else partialUserInfo.value = RelativeInfo()

        partialUserInfo.addSource(savedStateLiveData) {
            partialUserInfo.value = it as? RelativeInfo
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun updateUserInfo(newUserInfo: RelativeInfo) {
        handle.set(STATE_KEY, newUserInfo)
    }

    fun userInfo(): RelativeInfo? {
        return partialUserInfo.value
    }

    fun onAddRelativeComplete() {
        val userInfo = partialUserInfo.value!!
        uiScope.launch {
            loading.value = true
            delay(500)
            val result = apiManager.createFamilyMember(
                User(
                    id = "",
                    ageGroup = userInfo.ageGroup!!,
                    gender = userInfo.gender!!,
                    nickname = userInfo.nickname ,
                    isInSameHouse = userInfo.sameHouse
                )
            )
            loading.value = false
            if (result != null) {
                _navigateToMainPage.value = Event(true)
            } else {
                toast(AscoltoApplication.appContext.getString(R.string.server_generic_error))
            }
        }
    }

    fun onNextTap(nextFragment: Class<out RelativeContentFragment>) {
        _navigateToNextPage.value = Event(nextFragment)
    }

    fun onPrevTap() {
        _navigateToPrevPage.value = Event(true)
    }
}
