package org.immuni.android.ui.onboarding

import org.immuni.android.models.Gender
import org.immuni.android.models.AgeGroup
import java.io.Serializable

data class OnboardingUserInfo(
    var name: String? = null,
    var gender: Gender? = null,
    var ageGroup: AgeGroup? = null
): Serializable
