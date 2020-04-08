package com.bendingspoons.base.utils

import android.content.Context
import com.bendingspoons.base.R
import java.util.*

// PicoInfoProvider retrieve device and user specific info

interface DeviceInfoProvider {
    fun country(): String
    fun language(): String
    fun appLanguage(): String
    fun locale(): String
    fun timeZoneSecons(): Int
    fun timeZoneName(): String
    fun isDayLightSaving(): Boolean
    fun androidVersion(): String
    fun screenSize(context: Context): Double
    fun devicePlatform(): String
}

class DeviceInfoProviderImpl: DeviceInfoProvider {
    override fun country(): String {
        return Locale.getDefault().country
    }

    override fun language(): String {
        return Locale.getDefault().language
    }

    // so far on Android you cannot set app specific language like on iOS 13+
    override fun appLanguage(): String {
        return language()
    }

    override fun locale(): String {
        val l = Locale.getDefault()

        return if(l.script.isNullOrEmpty()) "${l.language}_${l.country}"
        else "${l.language}-${l.script}_${l.country}"
    }

    override fun timeZoneSecons(): Int {
        return TimeZone.getDefault().rawOffset / 1000
    }

    override fun timeZoneName(): String {
        return TimeZone.getDefault().id
    }

    override fun isDayLightSaving(): Boolean {
        return TimeZone.getDefault().inDaylightTime(Date())
    }

    override fun androidVersion(): String {
        return DeviceUtils.androidVersionAPI.toString()
    }

    override fun screenSize(context: Context): Double {
        return ScreenUtils.getScreenSizeInInches(context)
    }

    override fun devicePlatform(): String {
        val manufacturer = DeviceUtils.manufacturer ?: ""
        val model = DeviceUtils.model ?: ""
        return "$manufacturer $model".trim()
    }
}
