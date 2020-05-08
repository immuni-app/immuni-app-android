package org.immuni.android.ui.home.home.model

import org.immuni.android.models.survey.TriageProfile

// this represent an item in the home list

sealed class HomeItemType

data class SurveyCard(
    val doesMainUserNeedToLog: Boolean,
    val familyMembersThatNeedToLog: Int
) : HomeItemType() {
    val surveysToLog: Int = familyMembersThatNeedToLog + (if (doesMainUserNeedToLog) 1 else 0)
    var tapQuestion = false
}

class SurveyCardDone(val surveysLogged: Int) : HomeItemType()

class EnableGeolocationCard(
    val type: GeolocationType
) : HomeItemType()

class EnableBluetoothCard : HomeItemType()

class EnableNotificationCard : HomeItemType()

class AddToWhiteListCard : HomeItemType()

data class HeaderCard(
    val title: String
) : HomeItemType()

sealed class SuggestionsCard(
    val title: String,
    val triageProfile: TriageProfile
) : HomeItemType()

class SuggestionsCardWhite(
    title: String,
    triageProfile: TriageProfile
) : SuggestionsCard(title, triageProfile)

class SuggestionsCardYellow(
    title: String,
    triageProfile: TriageProfile
) : SuggestionsCard(title, triageProfile)

class SuggestionsCardRed(
    title: String,
    triageProfile: TriageProfile
) : SuggestionsCard(title, triageProfile)

enum class GeolocationType {
    PERMISSIONS, GLOBAL_GEOLOCATION
}
