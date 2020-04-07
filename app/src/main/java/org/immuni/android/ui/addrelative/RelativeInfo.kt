package org.immuni.android.ui.addrelative

import org.immuni.android.models.Gender
import org.immuni.android.models.AgeGroup
import org.immuni.android.models.Nickname
import java.io.Serializable

data class RelativeInfo(
    var name: String? = null,
    var gender: Gender? = null,
    var ageGroup: AgeGroup? = null,
    var sameHouse: Boolean? = null,
    var nickname: Nickname? = null,
    var hasSmartphone: Boolean? = null,
    var canAddInfoHimself: Boolean? = null,
    var alreadyAddedFromAnotherRelative: Boolean? = null
): Serializable
