package org.ascolto.onlus.geocrowd19.android.util

import androidx.annotation.BoolRes
import org.ascolto.onlus.geocrowd19.android.AscoltoApplication

fun featureEnabled(@BoolRes feature: Int): Boolean {
    return AscoltoApplication.appContext.resources.getBoolean(feature)
}