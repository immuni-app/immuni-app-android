package com.bendingspoons.ascolto.ui.onboarding

import com.bendingspoons.ascolto.db.entity.Gender
import java.io.Serializable
import java.util.*

data class OnboardingUserInfo(
    var name: String? = null,
    var gender: Gender? = null,
    var age: Int? = null
): Serializable
