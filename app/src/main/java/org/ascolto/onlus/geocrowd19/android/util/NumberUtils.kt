package org.ascolto.onlus.geocrowd19.android.util

object NumberUtils {

    fun roundToDecimals(number: Float, numDecimalPlaces: Int): Float {
        val factor = Math.pow(10.0, numDecimalPlaces.toDouble())
        return (Math.round(number * factor) / factor).toFloat()
    }
}