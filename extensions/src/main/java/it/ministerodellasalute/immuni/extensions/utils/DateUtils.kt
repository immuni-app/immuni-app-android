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

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000

    fun parseHttpDate(date: String) = SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        Locale.US
    ).parse(date)
}

fun Date.byAdding(
    year: Int = 0,
    days: Int = 0,
    hours: Int = 0,
    minutes: Int = 0,
    seconds: Int = 0,
    milliseconds: Int = 0,
    calendar: Calendar = Calendar.getInstance()
): Date {
    val date = this
    return calendar.apply {
        time = date
        if (year != 0) {
            add(Calendar.YEAR, year)
        }
        if (days != 0) {
            add(Calendar.DAY_OF_YEAR, days)
        }
        if (hours != 0) {
            add(Calendar.HOUR_OF_DAY, hours)
        }
        if (minutes != 0) {
            add(Calendar.MINUTE, minutes)
        }
        if (seconds != 0) {
            add(Calendar.SECOND, seconds)
        }
        if (milliseconds != 0) {
            add(Calendar.MILLISECOND, milliseconds)
        }
    }.time
}

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

val Date.isoDateString: String get() = dateFormatter.format(this)
val Date.isoDateTimeString: String get() = dateTimeFormatter.format(this)

fun String.formatDateString(date: String?): String? {
    return if (date != null) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)!!.isoDateString
    } else {
        null
    }
}

fun String.formatDateTimeString(date: String?): String? {
    return if (date != null) {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(date)!!.isoDateTimeString
    } else {
        null
    }
}
