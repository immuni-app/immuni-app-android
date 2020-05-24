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
import it.ministerodellasalute.immuni.logic.user.UserManager
import kotlinx.coroutines.*

class SetupViewModel(
    private val userManager: UserManager
) : ViewModel() {

    val navigateToMainPage = MutableLiveData<Event<Boolean>>()
    val navigateToWelcome = MutableLiveData<Event<Boolean>>()

    fun cancelInitializeJob() {
        initializeJob?.cancel()
    }

    var initializeJob: Job? = null
    fun initializeApp() {
        initializeJob?.cancel()
        initializeJob = viewModelScope.launch {
            delay(4000)
            navigateTo()
        }
    }

    private fun navigateTo() {
        if (!userManager.isWelcomeComplete.value || !userManager.isOnboardingComplete.value) {
            navigateToWelcome.value = Event(true)
        } else {
            navigateToMainPage.value = Event(true)
        }
    }
}
