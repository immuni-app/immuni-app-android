package com.bendingspoons.base.extensions

import kotlin.math.pow
import kotlin.math.round

fun Double.round(decimals: Int): Double {
    return round(this * 10.0.pow(decimals.toDouble())) / 10.0.pow(decimals.toDouble())
}