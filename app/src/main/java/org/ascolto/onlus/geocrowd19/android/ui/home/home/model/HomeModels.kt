package org.ascolto.onlus.geocrowd19.android.ui.home.home.model
// this represent an item in the home list

sealed class HomeItemType

data class SurveyCard(
    val active: Boolean,
    val surveyNumber: Int
): HomeItemType()

class EnableGeolocationCard: HomeItemType()

class EnableNotificationCard: HomeItemType()

class HeaderCard(
    val title: String
): HomeItemType()

class SuggestionsCard(
    val title: String,
    val severity: Severity
): HomeItemType()

enum class Severity {
    NONE, YELLOW, RED
}