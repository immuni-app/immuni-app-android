package org.immuni.android.ui.onboarding

import java.io.Serializable
import org.immuni.android.models.AgeGroup
import org.immuni.android.models.Gender

data class OnboardingUserInfo(
    var name: String? = null,
    var gender: Gender? = null,
    var ageGroup: AgeGroup? = null
) : Serializable
