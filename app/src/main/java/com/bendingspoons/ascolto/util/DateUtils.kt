package com.bendingspoons.ascolto.util

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun date(hours: Int, minutes: Int, ampm: String, is24hFormat: Boolean): Date {
        return when(is24hFormat) {
            true -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hours)
                    set(Calendar.MINUTE, minutes)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }
            else -> {
                val time = "$hours:$minutes $ampm"
                val date12Format = SimpleDateFormat("hh:mm a", Locale.US)
                // default
                var date = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 22)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                try {
                    date = date12Format.parse(time)
                } catch (e: Exception) {e.printStackTrace()}

                todayAtThisTime(date)
            }
        }
    }

    fun tomorrowAtThisTime(time: Date): Date {
        val _cal = Calendar.getInstance().apply {
            this.time = time
        }

        _cal.add(Calendar.HOUR, 24)

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _cal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, _cal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun todayAtThisTime(time: Date): Date {

        val _cal = Calendar.getInstance().apply {
            this.time = time
        }

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _cal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, _cal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
}