package org.immuni.android.util

import android.content.Context
import androidx.core.content.edit
import com.squareup.moshi.JsonAdapter
import org.immuni.android.ImmuniApplication
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

object Flags {
    const val ADD_FAMILY_MEMBER_DIALOG_SHOWN = "add_family_member_dialog_shown"
}

fun isFlagSet(flag: String): Boolean {
    val sharedPrefs = ImmuniApplication.appContext.getSharedPreferences(
        "IMMUNI_FLAGS",
        Context.MODE_PRIVATE
    )
    return sharedPrefs.getBoolean(flag, false)
}

fun setFlag(flag: String, value: Boolean = true) {
    val sharedPrefs = ImmuniApplication.appContext.getSharedPreferences(
        "IMMUNI_FLAGS",
        Context.MODE_PRIVATE
    )
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

inline fun <reified T : Any, K> toJson(obj: T, vararg adapters: JsonAdapter<K>): String {
    val moshi = adapters.fold(Moshi.Builder()) { builder, adapter -> builder.add(adapter) }
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

inline fun <reified T : Any, K> fromJson(json: String, vararg adapters: JsonAdapter<K>): T? {
    val moshi = adapters.fold(Moshi.Builder()) { builder, adapter -> builder.add(adapter) }
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    val jsonAdapter = moshi.adapter(T::class.java)
    return jsonAdapter.fromJson(json)
}
