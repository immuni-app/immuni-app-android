package org.ascolto.onlus.geocrowd19.android.ui.home

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
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
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.managers.BluetoothManager
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.managers.SurveyManager
import org.ascolto.onlus.geocrowd19.android.models.User
import org.ascolto.onlus.geocrowd19.android.models.survey.Severity
import org.ascolto.onlus.geocrowd19.android.models.survey.Severity.*
import org.ascolto.onlus.geocrowd19.android.toast
import org.ascolto.onlus.geocrowd19.android.ui.dialog.WebViewDialogActivity
import org.ascolto.onlus.geocrowd19.android.ui.home.family.model.*
import org.ascolto.onlus.geocrowd19.android.ui.home.home.model.*

import org.ascolto.onlus.geocrowd19.android.workers.BLEForegroundServiceWorker
import org.ascolto.onlus.geocrowd19.android.util.Flags
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeSharedViewModel(val database: AscoltoDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val oracle: Oracle<AscoltoSettings, AscoltoMe> by inject()
    private val surveyManager: SurveyManager by inject()
    private val apiManager: ApiManager by inject()
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

    val homelistModel = MutableLiveData<List<HomeItemType>>()
    val familylistModel = MutableLiveData<List<FamilyItemType>>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        refreshHomeListModel()
        startListenToMeModel()

        scheduleBLEWorker()
    }

    private fun scheduleBLEWorker() {
        val TAG = "BLE_WORKER"
        val workManager = WorkManager.getInstance(AscoltoApplication.appContext)
        workManager.cancelAllWorkByTag(TAG)

        val notificationWork = OneTimeWorkRequestBuilder<BLEForegroundServiceWorker>().addTag(TAG)
        workManager.enqueue(notificationWork.build())
    }

    private fun refreshHomeListModel() {
        uiScope.launch {
            oracle.me()?.let {
                val ctx = AscoltoApplication.appContext

                val itemsList = mutableListOf<HomeItemType>()

                // check geolocation/bluetooth disabled
                if (!GeolocationManager.hasAllPermissions(AscoltoApplication.appContext) ||
                    !bluetoothManager.isBluetoothEnabled()) {
                    itemsList.add(EnableGeolocationCard())
                }

                // check notifications disabled

                if (!PushNotificationUtils.areNotificationsEnabled(AscoltoApplication.appContext)) {
                    itemsList.add(EnableNotificationCard())
                }

                // survey card

                if (surveyManager.areAllSurveysLogged()) {
                    itemsList.add(SurveyCardDone())
                } else {
                    itemsList.add(SurveyCard(surveyManager.usersToLogCount()))
                }

                // suggestion cards

                val suggestionCards = mutableListOf<HomeItemType>()

                val survey = oracle.settings()?.survey

                for (user in surveyManager.allUsers()) {
                    val hasNeverCompletedSurveys = surveyManager.lastHealthProfile(user.id) == null
                    if (hasNeverCompletedSurveys) continue

                    val name =
                        if (user.isMain) ctx.resources.getString(R.string.you_as_complement) else user.name
                    val suggestionTitle = String.format(
                        ctx.resources.getString(R.string.indication_for),
                        "<b>$name</b>"
                    )
                    val triageProfileId = surveyManager.lastHealthProfile(user.id)?.triageProfileId
                    val triageProfile = triageProfileId?.let {
                        survey?.triage?.profile(it)
                    }
                    val severity = triageProfile?.severity ?: LOW
                    suggestionCards.add(
                        when (severity) {
                            LOW -> SuggestionsCardWhite(suggestionTitle, severity)
                            MID -> SuggestionsCardYellow(suggestionTitle, severity)
                            HIGH -> SuggestionsCardRed(suggestionTitle, severity)
                        }
                    )
                }

                if (suggestionCards.isNotEmpty()) {
                    itemsList.add(HeaderCard(ctx.resources.getString(R.string.home_separator_suggestions)))
                    itemsList.addAll(suggestionCards)
                }

                homelistModel.value = itemsList.toList()
            }
        }
    }

    private fun startListenToMeModel() {
        uiScope.launch {
            oracle.meFlow().collect { me ->

                val ctx = AscoltoApplication.appContext

                val itemsList = mutableListOf<FamilyItemType>()

                val mainUser = me.mainUser
                val familyMembers = me.familyMembers

                // add first main users
                mainUser?.let {
                    itemsList.add(UserCard(it))
                }

                // if there are family members, add header and all the members and a add button
                if (familyMembers.isNotEmpty())
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
        DeviceUtils.copyToClipBoard(AscoltoApplication.appContext, text = user.id)
        toast(AscoltoApplication.appContext.resources.getString(R.string.user_id_copied))
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
        val context = AscoltoApplication.appContext
        val intent = Intent(context, WebViewDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("url", url)
        }
        context.startActivity(intent)
    }
}
