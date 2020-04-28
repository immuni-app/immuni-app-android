package com.bendingspoons.base.extensions

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Round a double number to a given [decimals].
 */
fun Double.round(decimals: Int): Double {
    if(decimals <= 0) return this.roundToInt().toDouble()
    return round(this * 10.0.pow(decimals.toDouble())) / 10.0.pow(decimals.toDouble())
}