package org.ascolto.onlus.geocrowd19.android.ui.addrelative

import org.ascolto.onlus.geocrowd19.android.db.entity.Gender
import org.ascolto.onlus.geocrowd19.android.models.AgeGroup
import org.ascolto.onlus.geocrowd19.android.models.Nickname
import org.ascolto.onlus.geocrowd19.android.models.NicknameType
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
