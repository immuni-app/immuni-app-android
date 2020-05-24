/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.utils

import android.content.Context
import java.util.*

/**
 * Provides device specific info.
 */

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

class DeviceInfoProviderImpl : DeviceInfoProvider {
    override fun country(): String {
        return Locale.getDefault().country
    }

    override fun language(): String {
        return Locale.getDefault().language
    }

    override fun appLanguage(): String {
        return language()
    }

    override fun locale(): String {
        val l = Locale.getDefault()

        return if (l.script.isNullOrEmpty()) "${l.language}_${l.country}"
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
