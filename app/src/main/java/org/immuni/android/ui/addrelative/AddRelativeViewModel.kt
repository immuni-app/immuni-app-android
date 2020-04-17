package org.immuni.android.ui.addrelative

import androidx.lifecycle.*
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import kotlinx.coroutines.*
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.UserManager
import org.immuni.android.models.User
import org.immuni.android.ui.addrelative.fragment.profile.RelativeContentFragment
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class AddRelativeViewModel(val handle: SavedStateHandle, private val database: ImmuniDatabase) :
    ViewModel(), KoinComponent {

    companion object {
        const val STATE_KEY = "STATE_KEY"
    }

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val pico: Pico by inject()
    private val userManager: UserManager by inject()

    val mainUser = userManager.mainUser()!!

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
            //loading.value = true
            //delay(500)
            userManager.addUser(
                User(
                    ageGroup = userInfo.ageGroup!!,
                    gender = userInfo.gender!!,
                    nickname = userInfo.nickname,
                    isInSameHouse = userInfo.sameHouse
                )
            )
            //loading.value = false
            _navigateToMainPage.value = Event(true)
        }
    }

    fun onNextTap(nextFragment: Class<out RelativeContentFragment>) {
        _navigateToNextPage.value = Event(nextFragment)
    }

    fun onPrevTap() {
        _navigateToPrevPage.value = Event(true)
    }
}
