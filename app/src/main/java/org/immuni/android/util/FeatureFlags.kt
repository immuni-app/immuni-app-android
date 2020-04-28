package org.immuni.android.util

import androidx.annotation.BoolRes
import org.immuni.android.ImmuniApplication

fun featureEnabled(@BoolRes feature: Int): Boolean {
    return ImmuniApplication.appContext.resources.getBoolean(feature)
}