package com.bendingspoons.base.extensions

import java.math.BigInteger

fun ByteArray.toHex(): String {
    if (this.isEmpty()) return ""
    val bi = BigInteger(1, this)
    return String.format("%0" + (this.size shl 1) + "X", bi)
}
