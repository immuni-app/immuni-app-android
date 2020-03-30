package org.immuni.android.util

import androidx.annotation.BoolRes
import org.immuni.android.AscoltoApplication

fun featureEnabled(@BoolRes feature: Int): Boolean {
    return AscoltoApplication.appContext.resources.getBoolean(feature)
}