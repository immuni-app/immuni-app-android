/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.ui.setup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.ministerodellasalute.immuni.extensions.livedata.Event
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.user.UserManager
import kotlinx.coroutines.*

class SetupViewModel(
    private val userManager: UserManager,
    private val settingsManager: ConfigurationSettingsManager
) : ViewModel() {

    enum class Destination {
        Home, Welcome
    }

    val navigationDestination = MutableLiveData<Event<Destination>>()

    private var initializationJob: Job? = null

    fun cancelInitializationJob() {
        initializationJob?.cancel()
    }

    fun initializeApp() {
        initializationJob?.cancel()
        initializationJob = viewModelScope.launch {
            val minDelay = async {
                delay(4000)
            }
            val isFirstLaunch = !userManager.isSetupComplete.value
            if (isFirstLaunch) {
                // Let's fetch the configuration settings,
                // waiting no more than 10 seconds for them to download before proceeding
                val completion = CompletableDeferred<Unit>()
                async {
                    delay(10_000)
                    completion.complete(Unit)
                }
                async {
                    settingsManager.fetchSettingsAsync().await()
                    completion.complete(Unit)
                }
                completion.await()
            }
            minDelay.await()
            userManager.setSetupComplete(true)
            triggerNavigation()
        }
    }

    private fun triggerNavigation() {
        val destination =
            if (userManager.isWelcomeComplete.value && userManager.isOnboardingComplete.value)
                Destination.Home
            else
                Destination.Welcome

        navigationDestination.value = Event(destination)
    }
}
