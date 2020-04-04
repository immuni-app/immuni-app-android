package org.immuni.android.ui.home.home.model

import org.immuni.android.models.survey.Severity

// this represent an item in the home list

sealed class HomeItemType

data class SurveyCard(
    val doesMainUserNeedToLog: Boolean,
    val familyMembersThatNeedToLog: Int
): HomeItemType() {
    val surveysToLog: Int = familyMembersThatNeedToLog + (if (doesMainUserNeedToLog) 1 else 0)
    var tapQuestion = false
}

class SurveyCardDone(val surveysLogged: Int): HomeItemType()

class EnableGeolocationCard(
    val type: GeolocationType
): HomeItemType()

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

enum class GeolocationType {
    PERMISSIONS, GLOBAL_GEOLOCATION
}
