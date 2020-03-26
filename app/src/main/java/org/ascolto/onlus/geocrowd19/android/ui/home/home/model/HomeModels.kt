package org.ascolto.onlus.geocrowd19.android.ui.home.home.model

import org.ascolto.onlus.geocrowd19.android.models.survey.Severity

// this represent an item in the home list

sealed class HomeItemType

data class SurveyCard(
    val doesMainUserNeedToLog: Boolean,
    val familyMembersThatNeedToLog: Int
): HomeItemType() {
    val surveysToLog: Int = familyMembersThatNeedToLog + (if (doesMainUserNeedToLog) 1 else 0)
}

class SurveyCardDone: HomeItemType()

class EnableGeolocationCard: HomeItemType()

class EnableBluetoothCard: HomeItemType()

class EnableNotificationCard: HomeItemType()

data class HeaderCard(
    val title: String
): HomeItemType()

data class SuggestionsCardWhite(
    val title: String,
    val severity: Severity
): HomeItemType()

data class SuggestionsCardYellow(
    val title: String,
    val severity: Severity
): HomeItemType()

data class SuggestionsCardRed(
    val title: String,
    val severity: Severity
): HomeItemType()
