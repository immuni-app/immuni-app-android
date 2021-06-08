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

package it.ministerodellasalute.immuni.ui.home

import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus

/**
 * Represents an item in the home list.
 */

sealed class HomeItemType

class ProtectionCard(
    val active: Boolean,
    val status: ExposureStatus
) : HomeItemType()

data class SectionHeader(
    val title: String
) : HomeItemType()

sealed class InformationCard : HomeItemType()

object HowItWorksCard : InformationCard()
object SelfCareCard : InformationCard()
object CountriesOfInterestCard : InformationCard()
object ReportPositivityCard : InformationCard()
object GreenPassCard : InformationCard()

data class DisableExposureApi(val isEnabled: Boolean) : HomeItemType()
