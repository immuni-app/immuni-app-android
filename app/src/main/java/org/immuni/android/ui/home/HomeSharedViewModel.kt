package org.immuni.android.ui.home

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.extensions.livedata.Event
import org.immuni.android.extensions.utils.DeviceUtils
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.R
import org.immuni.android.api.APIManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.UserManager
import org.immuni.android.models.User
import org.immuni.android.models.survey.TriageProfile
import org.immuni.android.extensions.activity.toast
import org.immuni.android.ui.dialog.WebViewDialogActivity
import org.immuni.android.ui.home.family.model.*
import org.immuni.android.ui.home.home.model.*
import org.immuni.android.ui.onboarding.Onboarding

import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeSharedViewModel(val database: ImmuniDatabase) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val api: APIManager by inject()
    private val userManager: UserManager by inject()
    private val surveyManager: SurveyManager by inject()
    private val bluetoothManager: BluetoothManager by inject()
    private val onboarding: Onboarding by inject()

    private val _showAddFamilyMemberDialog = MutableLiveData<Event<Boolean>>()
    val showAddFamilyMemberDialog: LiveData<Event<Boolean>>
        get() = _showAddFamilyMemberDialog

    private val _showSuggestionDialog = MutableLiveData<Event<TriageProfile>>()
    val showSuggestionDialog: LiveData<Event<TriageProfile>>
        get() = _showSuggestionDialog

    private val _navigateToSurvey = MutableLiveData<Event<Boolean>>()
    val navigateToSurvey: LiveData<Event<Boolean>>
        get() = _navigateToSurvey

    private val _selectFamilyTab = MutableLiveData<Event<Boolean>>()
    val selectFamilyTab: LiveData<Event<Boolean>>
        get() = _selectFamilyTab

    val homelistModel = MutableLiveData<List<HomeItemType>>()
    val familylistModel = MediatorLiveData<List<FamilyItemType>>()
    val blockingItemsListModel = MutableLiveData<List<HomeItemType>>()

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
            val ctx = ImmuniApplication.appContext

            val itemsList = mutableListOf<HomeItemType>()

            val blockingList = mutableListOf<HomeItemType>()

            // check bluetooth disabled

            if (!bluetoothManager.isBluetoothSupported() || !bluetoothManager.isBluetoothEnabled()) {
                blockingList.add(EnableBluetoothCard())
            }

            // check notifications disabled

            if (!PushNotificationUtils.areNotificationsEnabled(ImmuniApplication.appContext)) {
                blockingList.add(EnableNotificationCard())
            }

            blockingItemsListModel.value = blockingList

            // survey card

            // suggestion cards
            /*
            itemsList.add(
                when (triageProfile.severity) {
                    LOW -> SuggestionsCardWhite(suggestionTitle, triageProfile)
                    MID -> SuggestionsCardYellow(suggestionTitle, triageProfile)
                    HIGH -> SuggestionsCardRed(suggestionTitle, triageProfile)
                }
            )
             */


            homelistModel.value = itemsList.toList()
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

            /*
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
             */

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
        //checkAddFamilyMembersDialog()
    }

    // check if this one shot dialog has been alredy triggered before
    // if not show it
    private fun checkAddFamilyMembersDialog() {
        if (!onboarding.familyDialogShown()) {
            uiScope.launch {
                _showAddFamilyMemberDialog.value = Event(true)
                onboarding.setFamilyDialogShown(true)
            }
        }
    }

    fun openSuggestions(triageProfile: TriageProfile) {
        _showSuggestionDialog.value = Event(triageProfile)
    }

    fun onUserIdTap(user: User) {
        // copy to clipboard
        DeviceUtils.copyToClipBoard(ImmuniApplication.appContext, text = user.id)
        toast(
            ImmuniApplication.appContext,
            ImmuniApplication.appContext.resources.getString(
                R.string.user_id_copied
            )
        )
    }

    fun onPrivacyPolicyClick() {
        api.latestSettings()?.privacyPolicyUrl?.let {
            openUrlInDialog(it)
        }
    }

    fun onFaqClick() {
        api.latestSettings()?.faqUrl?.let {
            openUrlInDialog(it)
        }
    }

    fun onTosClick() {
        api.latestSettings()?.termsOfServiceUrl?.let {
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
