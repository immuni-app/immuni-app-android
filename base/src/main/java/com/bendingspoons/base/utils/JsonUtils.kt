package com.bendingspoons.base.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*
import kotlin.reflect.KClass

interface JSonSerializable {
    fun onDeserialize() {}
    fun onSerialize(json: String) = json
}

inline fun <reified T : Any> toJson(obj: T): String {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    val jsonAdapter = moshi.adapter(T::class.java)
    var json = jsonAdapter.toJson(obj)
    if (obj is JSonSerializable) {
        json = obj.onSerialize(json)
    }
    return json
}

inline fun <reified T : Any> fromJson(json: String, lenient: Boolean = false): T? {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    var jsonAdapter = moshi.adapter(T::class.java)
    if (lenient) {
        jsonAdapter = jsonAdapter.lenient()
    }
    val obj = jsonAdapter.fromJson(json)
    if (obj is JSonSerializable) {
        obj.onDeserialize()
    }
    return obj
}

fun <T : Any> toJson(objectClass: KClass<T>, obj: T): String {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    val jsonAdapter = moshi.adapter(objectClass.java)
    var json = jsonAdapter.toJson(obj)
    if (obj is JSonSerializable) {
        json = obj.onSerialize(json)
    }
    return json
}

fun <T : Any> fromJson(objectClass: KClass<T>, json: String, lenient: Boolean = false): T? {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()
    var jsonAdapter = moshi.adapter(objectClass.java)
    if (lenient) {
        jsonAdapter = jsonAdapter.lenient()
    }
    val obj = jsonAdapter.fromJson(json)
    if (obj is JSonSerializable) {
        obj.onDeserialize()
    }
    return obj
}
