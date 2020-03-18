package com.bendingspoons.ascolto.util

import androidx.annotation.BoolRes
import com.bendingspoons.ascolto.AscoltoApplication

fun featureEnabled(@BoolRes feature: Int): Boolean {
    return AscoltoApplication.appContext.resources.getBoolean(feature)
}