package com.bendingspoons.base.crypto

import android.content.Context

/**
 * Secured shared preference for strings
 * Tha value (not the key) is crypted.
 */
object SecuredSharedPreferences {

    private val TAG = "SecuredSharedPreferences"

    fun save(context: Context, key: String, value: String) {
        //SharedPreferencesHelper.save(context, CryptoAlgorithmWrapper.prefix+key, CryptoAlgorithmWrapper.encrypt(value))
    }


    fun get(context: Context, key: String): String? {
        return ""//CryptoAlgorithmWrapper.decrypt(SharedPreferencesHelper.get(context, CryptoAlgorithmWrapper.prefix+key))
    }


    fun get(context: Context, key: String, defValue: String): String? {
        val value = ""//SharedPreferencesHelper.get(context, CryptoAlgorithmWrapper.prefix+key, defValue)
        return if (value == null || value == defValue)
            value
        else
            CryptoAlgorithmWrapper.decrypt(value)
    }


    fun clear(context: Context, key: String) {
        //SharedPreferencesHelper.clear(context, CryptoAlgorithmWrapper.prefix+key)
    }
}
