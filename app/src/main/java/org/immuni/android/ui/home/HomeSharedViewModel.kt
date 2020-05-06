package org.immuni.android.ui.home

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.immuni.android.extensions.livedata.Event
import kotlinx.coroutines.*
import org.immuni.android.ImmuniApplication
import org.immuni.android.data.SettingsDataSource
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.UserManager
import org.immuni.android.models.survey.TriageProfile
import org.immuni.android.ui.dialog.WebViewDialogActivity
import org.immuni.android.ui.home.home.model.*

import org.koin.core.KoinComponent

class HomeSharedViewModel(
    val settings: SettingsDataSource,
    val bluetoothManager: BluetoothManager
) : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val homelistModel = MutableLiveData<List<HomeItemType>>()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        refreshHomeListModel()
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

    fun onHomeResumed() {
        refreshHomeListModel()
        //checkAddFamilyMembersDialog()
    }

    fun onPrivacyPolicyClick() {
        settings.latestSettings()?.privacyPolicyUrl?.let {
            openUrlInDialog(it)
        }
    }

    fun onTosClick() {
        settings.latestSettings()?.termsOfServiceUrl?.let {
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
}
