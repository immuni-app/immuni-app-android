package org.ascolto.onlus.geocrowd19.android.util

import java.util.*

object UserLocale {

    private val SUPPORTED_LOCALES = listOf("en")

    fun locale(): String {
        val language = Locale.getDefault().language.toLowerCase(Locale.ROOT)
        return if(language in SUPPORTED_LOCALES) language
        else "en"
    }
}