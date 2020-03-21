package org.ascolto.onlus.geocrowd19.android.util

import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object StringUtils {
    fun millisToFormattedTime(millis: Long): String {
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))).trim()
    }

    fun secondsToFormattedTime(seconds: Int): String {
        val seconds = seconds.toLong()
        return String.format("%02d:%02d",
            TimeUnit.SECONDS.toMinutes(seconds) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds)),
            TimeUnit.SECONDS.toSeconds(seconds) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds))).trim()
    }

    fun secondsToRoundedMinutes(seconds: Long): String {
        val minutes = seconds.toFloat() / 60f
        return String.format("%3d", minutes.roundToInt()).trim()
    }

    fun secondsToRoundedMinutes(seconds: Double): Int {
        return (seconds.toFloat() / 60f).toInt()
    }

}