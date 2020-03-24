package org.ascolto.onlus.geocrowd19.android.ui.onboarding

import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.models.AgeGroup
import java.io.Serializable
import java.util.*

data class OnboardingUserInfo(
    var name: String? = null,
    var gender: Gender? = null,
    var ageGroup: AgeGroup? = null
): Serializable
