package org.ascolto.onlus.geocrowd19.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.util.isFlagSet
import org.ascolto.onlus.geocrowd19.android.util.setFlag
import com.bendingspoons.base.livedata.Event
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.getSettingsSurvey
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.models.survey.Severity
import org.ascolto.onlus.geocrowd19.android.ui.home.home.HomeListAdapter
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeSharedViewModel(val database: AscoltoDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _showAddFamilyMemberDialog = MutableLiveData<Event<Boolean>>()
    val showAddFamilyMemberDialog: LiveData<Event<Boolean>>
        get() = _showAddFamilyMemberDialog

    private val _showSuggestionDialog = MutableLiveData<Event<String>>()
    val showSuggestionDialog: LiveData<Event<String>>
        get() = _showSuggestionDialog

    val listModel = MutableLiveData<List<HomeItemType>>()

    init {
        refreshListModel()
    }

    private fun refreshListModel() {
        uiScope.launch {
            val model = database.userInfoDao().getMainUserInfoFlow().collect {
                val ctx = AscoltoApplication.appContext

                val itemsList = mutableListOf<HomeItemType>()

                // check geolocation disabled

                if(!GeolocationManager.hasAllPermissions(AscoltoApplication.appContext)) {
                    itemsList.add(EnableGeolocationCard())
                }

                // check notifications disabled

                if(!PushNotificationUtils.areNotificationsEnabled(AscoltoApplication.appContext)) {
                    itemsList.add(EnableNotificationCard())
                }

                // fake cards
                val suggestionTitle = String.format(ctx.resources.getString(R.string.indication_for),
                    "<b>Giulia</b>")
                val suggestionTitle2 = String.format(ctx.resources.getString(R.string.indication_for),
                    "<b>Marco e Matteo</b>")
                val suggestionTitle3 = String.format(ctx.resources.getString(R.string.indication_for),
                    "<b>Roddy</b>")
                itemsList.add(SurveyCard(true, 1))
                itemsList.add(HeaderCard("ciao"))
                itemsList.add(SuggestionsCard(suggestionTitle, Severity.LOW))
                itemsList.add(SuggestionsCard(suggestionTitle2, Severity.MID))
                itemsList.add(SuggestionsCard(suggestionTitle3, Severity.HIGH))

                listModel.value = itemsList.toList()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onHomeResumed() {
        refreshListModel()
        checkAddFamilyMembersDialog()
    }

    // check if this one shot dialog has been alredy triggered before
    // if not show it
    private fun checkAddFamilyMembersDialog() {
        val flag = "family_add_member_popup_showed"
        uiScope.launch {
            delay(500)
            if(!isFlagSet(flag)) {
                _showAddFamilyMemberDialog.value = Event(true)
                setFlag(flag, true)
            }
        }
    }

    fun openSuggestions(severity: Severity) {
        val survey = getSettingsSurvey()?.survey()
        survey?.let { s ->
            val url = s.triage.profiles.firstOrNull { it.severity == severity }?.url
            url?.let { _showSuggestionDialog.value = Event(it) }
        }
    }
}
