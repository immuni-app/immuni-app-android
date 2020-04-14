package org.immuni.android.ui.home

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.util.isFlagSet
import org.immuni.android.util.setFlag
import com.bendingspoons.base.livedata.Event
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.oracle.Oracle
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.UserManager
import org.immuni.android.models.User
import org.immuni.android.models.survey.Severity
import org.immuni.android.models.survey.Severity.*
import org.immuni.android.toast
import org.immuni.android.ui.dialog.WebViewDialogActivity
import org.immuni.android.ui.home.family.model.*
import org.immuni.android.ui.home.home.model.*

import org.immuni.android.util.Flags
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeSharedViewModel(val database: ImmuniDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val oracle: Oracle<ImmuniSettings, ImmuniMe> by inject()
    private val userManager: UserManager by inject()
    private val surveyManager: SurveyManager by inject()
    private val bluetoothManager: BluetoothManager by inject()

    private val _showAddFamilyMemberDialog = MutableLiveData<Event<Boolean>>()
    val showAddFamilyMemberDialog: LiveData<Event<Boolean>>
        get() = _showAddFamilyMemberDialog

    private val _showSuggestionDialog = MutableLiveData<Event<Pair<String, Severity>>>()
    val showSuggestionDialog: LiveData<Event<Pair<String, Severity>>>
        get() = _showSuggestionDialog

    private val _navigateToSurvey = MutableLiveData<Event<Boolean>>()
    val navigateToSurvey: LiveData<Event<Boolean>>
        get() = _navigateToSurvey

    private val _selectFamilyTab = MutableLiveData<Event<Boolean>>()
    val selectFamilyTab: LiveData<Event<Boolean>>
        get() = _selectFamilyTab

    val homelistModel = MutableLiveData<List<HomeItemType>>()
    val familylistModel = MediatorLiveData<List<FamilyItemType>>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        refreshHomeListModel()
        startListeningToUsers()

        bluetoothManager.scheduleBLEWorker(ImmuniApplication.appContext)
    }

    private fun refreshHomeListModel() {
        uiScope.launch {
            oracle.me()?.let {
                val ctx = ImmuniApplication.appContext

                val itemsList = mutableListOf<HomeItemType>()

                // check bluetooth disabled

                if (!bluetoothManager.isBluetoothSupported() || !bluetoothManager.isBluetoothEnabled()) {
                    itemsList.add(EnableBluetoothCard())
                }

                // check geolocation disabled

                // only show one geolocation card at the time in order to not have too many cards
                if (!PermissionsManager.hasAllPermissions(ImmuniApplication.appContext)) {
                    itemsList.add(EnableGeolocationCard(GeolocationType.PERMISSIONS))
                }
                /*else if(!PermissionsManager.globalLocalisationEnabled(ImmuniApplication.appContext)) {
                    itemsList.add(EnableGeolocationCard(GeolocationType.GLOBAL_GEOLOCATION))
                }*/

                // check notifications disabled

                if (!PushNotificationUtils.areNotificationsEnabled(ImmuniApplication.appContext)) {
                    itemsList.add(EnableNotificationCard())
                }

                // survey card

                if (surveyManager.areAllSurveysLogged()) {
                    itemsList.add(SurveyCardDone(userManager.users().size))
                } else {
                    val mainUser = userManager.mainUser()!!
                    val familyMembers = userManager.familyMembers()
                    itemsList.add(
                        SurveyCard(
                            !surveyManager.isSurveyCompletedForUser(mainUser.id),
                            familyMembers.filter {
                                !surveyManager.isSurveyCompletedForUser(it.id)
                            }.count()
                        )
                    )
                }

                // suggestion cards
                val survey = oracle.settings()?.survey

                val userCardsMap = mutableMapOf<Severity, MutableList<String>>()
                for (user in userManager.users()) {
                    val hasNeverCompletedSurveys = surveyManager.lastHealthProfile(user.id) == null
                    if (hasNeverCompletedSurveys) continue

                    val name =
                        if (user.isMain) ctx.resources.getString(R.string.you_as_complement) else user.name

                    val triageProfileId = surveyManager.lastHealthProfile(user.id)?.triageProfileId
                    val triageProfile = triageProfileId?.let {
                        survey?.triage?.profile(it)
                    }
                    val severity = triageProfile?.severity ?: LOW

                    userCardsMap[severity] = (userCardsMap[severity] ?: mutableListOf()).apply {
                        add(name)
                    }
                }

                if (userCardsMap.keys.isNotEmpty()) {
                    itemsList.add(HeaderCard(ctx.resources.getString(R.string.home_separator_suggestions)))
                    userCardsMap.keys.forEach { severity ->
                        val and = ImmuniApplication.appContext.getString(R.string.and)
                        val names: String
                        val namesList = userCardsMap[severity]!!
                        if (namesList.size == 1) {
                            names = namesList.first()
                        } else {
                            // remove last
                            val lastItem = namesList.removeAt(namesList.size - 1)
                            names = "${namesList.joinToString(separator = ", ")} $and $lastItem"
                        }


                        val suggestionTitle = String.format(
                            ctx.resources.getString(R.string.indication_for),
                            "<b>$names</b>"
                        )

                        itemsList.add(
                            when (severity) {
                                LOW -> SuggestionsCardWhite(suggestionTitle, severity)
                                MID -> SuggestionsCardYellow(suggestionTitle, severity)
                                HIGH -> SuggestionsCardRed(suggestionTitle, severity)
                            }
                        )
                    }
                }

                homelistModel.value = itemsList.toList()
            }
        }
    }

    private fun startListeningToUsers() {
        familylistModel.addSource(userManager.usersLiveData()) { users ->
            val ctx = ImmuniApplication.appContext

            val itemsList = mutableListOf<FamilyItemType>()

            val mainUser = users.find { it.isMain }
            val familyMembers = users.filter { !it.isMain }

            // add first main users
            mainUser?.let {
                itemsList.add(UserCard(it, 0))
            }

            // if there are family members, add header and all the members and a add button
            if (familyMembers.isNotEmpty()) {
                itemsList.add(FamilyHeaderCard(ctx.resources.getString(R.string.your_family_members_separator)))
                familyMembers.forEachIndexed { index, user ->
                    itemsList.add(UserCard(user, index + 1))
                }
                itemsList.add(AddFamilyMemberButtonCard())
            }
            // otherwise add only the add member tutorial button
            else {
                itemsList.add(AddFamilyMemberTutorialCard())
            }

            familylistModel.postValue(itemsList)
        }
    }

    fun onSurveyCardTap() {
        uiScope.launch {
            if (surveyManager.areAllSurveysLogged()) {
                // All users already took the survey for today!
                return@launch
            }

            _navigateToSurvey.value = Event(true)
        }
    }

    fun onHomeResumed() {
        refreshHomeListModel()
        checkAddFamilyMembersDialog()
    }

    // check if this one shot dialog has been alredy triggered before
    // if not show it
    private fun checkAddFamilyMembersDialog() {
        val flag = Flags.ADD_FAMILY_MEMBER_DIALOG_SHOWN
        if (!isFlagSet(flag)) {
            uiScope.launch {
                _showAddFamilyMemberDialog.value = Event(true)
                setFlag(flag, true)
            }
        }
    }

    fun openSuggestions(severity: Severity) {
        val survey = oracle.settings()?.survey
        survey?.let { s ->
            val url = s.triage.profiles.firstOrNull { it.severity == severity }?.url
            url?.let { _showSuggestionDialog.value = Event(Pair(it, severity)) }
        }
    }

    fun onUserIdTap(user: User) {
        // copy to clipboard
        DeviceUtils.copyToClipBoard(ImmuniApplication.appContext, text = user.id)
        toast(ImmuniApplication.appContext.resources.getString(R.string.user_id_copied))
    }

    fun onPrivacyPolicyClick() {
        oracle.settings()?.privacyPolicyUrl?.let {
            openUrlInDialog(it)
        }
    }

    fun onFaqClick() {
        oracle.settings()?.faqUrl?.let {
            openUrlInDialog(it)
        }
    }

    fun onTosClick() {
        oracle.settings()?.termsOfServiceUrl?.let {
            openUrlInDialog(it)
        }
    }

    private fun openUrlInDialog(url: String) {
        val context = ImmuniApplication.appContext
        val intent = Intent(context, WebViewDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("url", url)
        }
        context.startActivity(intent)
    }

    fun selectFamilyTab() {
        _selectFamilyTab.value = Event(true)
    }
}
