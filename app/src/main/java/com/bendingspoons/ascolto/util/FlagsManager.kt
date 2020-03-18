package com.bendingspoons.ascolto.util

import android.content.Context
import androidx.core.content.edit
import com.bendingspoons.ascolto.AscoltoApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

const val FLAG_MUTE_LIVE_VIEW = "FLAG_MUTE_LIVE_VIEW"

fun isFlagSet(flag: String): Boolean {
    val sharedPrefs = AscoltoApplication.appContext.getSharedPreferences("SLEEP_FLAGS", Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean(flag, false)
}

fun setFlag(flag: String, value: Boolean = true) {
    val sharedPrefs = AscoltoApplication.appContext.getSharedPreferences("SLEEP_FLAGS", Context.MODE_PRIVATE)
    sharedPrefs.edit {
            putBoolean(flag, value)
        }
}

inline fun <reified T : Any> toJson(obj: T): String {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    val jsonAdapter = moshi.adapter(T::class.java)
    return jsonAdapter.toJson(obj)
}

inline fun <reified T : Any> fromJson(json: String): T? {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    val jsonAdapter = moshi.adapter(T::class.java)
    return jsonAdapter.fromJson(json)
}
