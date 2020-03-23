package org.ascolto.onlus.geocrowd19.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.util.isFlagSet
import org.ascolto.onlus.geocrowd19.android.util.setFlag
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication
import org.ascolto.onlus.geocrowd19.android.R
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.managers.SurveyManager
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.models.survey.Severity
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.*
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.EnableGeolocationCard
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.EnableNotificationCard
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.HeaderCard
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.HomeItemType
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.SuggestionsCardRed
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.SuggestionsCardWhite
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.SuggestionsCardYellow
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.SurveyCard
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.SurveyCardDone
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeSharedViewModel(val database: AscoltoDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val surveyManager: SurveyManager by inject()

    private val _showAddFamilyMemberDialog = MutableLiveData<Event<Boolean>>()
    val showAddFamilyMemberDialog: LiveData<Event<Boolean>>
        get() = _showAddFamilyMemberDialog

    private val _showSuggestionDialog = MutableLiveData<Event<String>>()
    val showSuggestionDialog: LiveData<Event<String>>
        get() = _showSuggestionDialog

    private val _navigateToSurvey = MutableLiveData<Event<Boolean>>()
    val navigateToSurvey: LiveData<Event<Boolean>>
        get() = _navigateToSurvey

    val homelistModel = MutableLiveData<List<HomeItemType>>()
    val familylistModel = MutableLiveData<List<FamilyItemType>>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        refreshHomeListModel()
        startListenToMeModel()
    }

    private fun refreshHomeListModel() {
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

                // survey card

                if(surveyManager.areAllSurveysLogged()) {
                    itemsList.add(SurveyCardDone())
                } else {
                    itemsList.add(SurveyCard(surveyManager.usersToLogSize()))
                }

                // suggestions card

                // TODO remove this header is there are no suggestions card yet (at startup?)
                itemsList.add(HeaderCard(ctx.resources.getString(R.string.home_separator_suggestions)))

                // TODO for each user (or group uf users by severity) create a suggestion card
                // fake cards
                val suggestionTitle = String.format(ctx.resources.getString(R.string.indication_for),
                    "<b>Giulia</b>")
                val suggestionTitle2 = String.format(ctx.resources.getString(R.string.indication_for),
                    "<b>Marco e Matteo</b>")
                val suggestionTitle3 = String.format(ctx.resources.getString(R.string.indication_for),
                    "<b>Roddy</b>")

                itemsList.add(SuggestionsCardWhite(suggestionTitle, Severity.LOW))
                itemsList.add(SuggestionsCardYellow(suggestionTitle2, Severity.MID))
                itemsList.add(SuggestionsCardRed(suggestionTitle3, Severity.HIGH))

                homelistModel.value = itemsList.toList()
            }
        }
    }

    private fun startListenToMeModel() {
        uiScope.launch {
            oracle.meFlow().collect { me ->
                refreshHomeListModel()
                
                val ctx = AscoltoApplication.appContext

                val itemsList = mutableListOf<FamilyItemType>()

                val mainUser = me.mainUser
                val familyMembers = me.familyMembers

                // add first main users
                mainUser?.let {
                    itemsList.add(UserCard(it))
                }

                // if there are family members, add header and all the members and a add button
                if(familyMembers.isNotEmpty())
                    familyMembers.let { members ->
                        itemsList.add(FamilyHeaderCard(ctx.resources.getString(R.string.your_family_members_separator)))
                        familyMembers.forEach {
                            itemsList.add(UserCard(it))
                        }
                        itemsList.add(AddFamilyMemberButtonCard())
                    }
                // otherwise add only the add member tutorial button
                else {
                    itemsList.add(AddFamilyMemberTutorialCard())
                }

                // TODO REMOVE
                itemsList.add(FamilyHeaderCard(ctx.resources.getString(R.string.your_family_members_separator)))

                mainUser?.let {
                    itemsList.add(UserCard(it))
                }
                mainUser?.let {
                    itemsList.add(UserCard(it))
                }
                itemsList.add(AddFamilyMemberButtonCard())

                familylistModel.value = itemsList.toList()
            }
        }
    }

    fun onSurveyCardTap() {
        if (surveyManager.areAllSurveysLogged()) {
            // All users already took the survey for today!
            return
        }

        _navigateToSurvey.value = Event(true)
    }

    fun onHomeResumed() {
        refreshHomeListModel()
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
        val survey = oracle.settings()?.survey
        survey?.let { s ->
            val url = s.triage.profiles.firstOrNull { it.severity == severity }?.url
            url?.let { _showSuggestionDialog.value = Event(it) }
        }
    }

    fun onUserIdTap(user: User) {
        // copy to clipboard
        DeviceUtils.copyToClipBoard(AscoltoApplication.appContext, text = user.id)
        toast(AscoltoApplication.appContext.resources.getString(R.string.user_id_copied))
    }
}
