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

package it.ministerodellasalute.immuni.logic.user

import it.ministerodellasalute.immuni.logic.user.models.Region
import it.ministerodellasalute.immuni.logic.user.models.User
import it.ministerodellasalute.immuni.logic.user.repositories.RegionRepository
import it.ministerodellasalute.immuni.logic.user.repositories.UserRepository
import org.koin.core.KoinComponent

class UserManager(
    private val userRepository: UserRepository,
    private val regionRepository: RegionRepository
) : KoinComponent {
    // region: Setup

    val isSetupComplete = userRepository.isSetupComplete

    fun setSetupComplete(complete: Boolean) {
        userRepository.setSetupComplete(complete)
    }

    // endregion

    // region: Welcome

    val isWelcomeComplete = userRepository.isWelcomeComplete

    fun setWelcomeComplete(complete: Boolean) {
        userRepository.setWelcomeComplete(complete)
    }

    // endregion

    // region: Onboarding

    val isOnboardingComplete = userRepository.isOnboardingComplete

    fun setOnboardingComplete(complete: Boolean) {
        userRepository.setOnboardingComplete(complete)
    }

    // endregion

    // region: User

    val user = userRepository.user

    fun save(user: User) {
        userRepository.save(user)
    }

    fun regions(): List<Region> = regionRepository.regions()

    fun provinces(region: Region) = regionRepository.provinces(region = region)

    // endregion
}
