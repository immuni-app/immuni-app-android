package org.ascolto.onlus.geocrowd19.android.ui.home.home.model

import org.ascolto.onlus.geocrowd19.android.models.survey.Severity

// this represent an item in the home list

sealed class HomeItemType

data class SurveyCard(
    val surveyNumber: Int
): HomeItemType()

class SurveyCardDone: HomeItemType()

class EnableGeolocationCard: HomeItemType()

class EnableNotificationCard: HomeItemType()

class HeaderCard(
    val title: String
): HomeItemType()

class SuggestionsCardWhite(
    val title: String,
    val severity: Severity
): HomeItemType()

class SuggestionsCardYellow(
    val title: String,
    val severity: Severity
): HomeItemType()

class SuggestionsCardRed(
    val title: String,
    val severity: Severity
): HomeItemType()
