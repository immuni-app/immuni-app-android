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

package it.ministerodellasalute.immuni.logic.user.repositories

import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.logic.user.models.User

class UserRepository(
    private val storage: KVStorage
) {
    companion object {
        private val userKey = KVStorage.Key<User>("User")
        private val setupCompleteKey = KVStorage.Key<Boolean>("SetupComplete")
        private val welcomeCompleteKey = KVStorage.Key<Boolean>("WelcomeComplete")
        private val onboardingCompleteKey = KVStorage.Key<Boolean>("OnboardingComplete")
    }

    val user = storage.stateFlow(userKey)

    fun save(user: User) {
        storage[userKey] = user
    }

    val isSetupComplete = storage.stateFlow(setupCompleteKey, defaultValue = false)

    fun setSetupComplete(complete: Boolean) {
        storage[setupCompleteKey] = complete
    }

    val isWelcomeComplete = storage.stateFlow(welcomeCompleteKey, defaultValue = false)

    fun setWelcomeComplete(complete: Boolean) {
        storage[welcomeCompleteKey] = complete
    }

    val isOnboardingComplete = storage.stateFlow(onboardingCompleteKey, defaultValue = false)

    fun setOnboardingComplete(complete: Boolean) {
        storage[onboardingCompleteKey] = complete
    }
}
