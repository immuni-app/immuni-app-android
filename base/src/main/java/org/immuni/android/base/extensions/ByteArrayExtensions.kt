package org.immuni.android.base.extensions

import java.math.BigInteger

/**
 * Convert a byte array to hexadecimal format.
 */
fun ByteArray.toHex(): String {
    if (this.isEmpty()) return ""
    val bi = BigInteger(1, this)
    return String.format("%0" + (this.size shl 1) + "X", bi)
}
