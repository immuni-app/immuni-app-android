/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.extensions.utils

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KClass

private fun Moshi.Builder.addBaseAdapters(): Moshi.Builder {
    add(KotlinJsonAdapterFactory())
    add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
    return this
}

val defaultMoshi: Moshi = Moshi.Builder().addBaseAdapters().build()

fun moshi(
    extraAdapters: Map<KClass<*>, JsonAdapter<*>> = mapOf(),
    extraFactories: List<JsonAdapter.Factory> = listOf()
): Moshi = Moshi.Builder().apply {
    extraAdapters.forEach { add(it.key.java, it.value) }
    extraFactories.forEach { add(it) }
    addBaseAdapters()
}.build()

inline fun <reified T : Any> Moshi.toJson(
    obj: T
): String {
    val jsonAdapter = adapter(T::class.java)
    return jsonAdapter.toJson(obj)
}

fun <T : Any> Moshi.toJson(
    objectClass: KClass<T>,
    obj: T
): String {
    val jsonAdapter = adapter(objectClass.java)
    return jsonAdapter.toJson(obj)
}

inline fun <reified T : Any> Moshi.fromJson(
    json: String,
    lenient: Boolean = false
): T? {
    var jsonAdapter = adapter(T::class.java)
    if (lenient) {
        jsonAdapter = jsonAdapter.lenient()
    }
    return jsonAdapter.fromJson(json)
}

fun <T : Any> Moshi.fromJson(
    objectClass: KClass<T>,
    json: String,
    lenient: Boolean = false
): T? {
    var jsonAdapter = adapter(objectClass.java)
    if (lenient) {
        jsonAdapter = jsonAdapter.lenient()
    }
    return jsonAdapter.fromJson(json)
}

fun loadJsonAsset(context: Context, path: String): String? {
    var ins: InputStream? = null
    try {
        ins = context.assets.open(path)
        val size = ins.available()
        val buffer = ByteArray(size)
        ins.read(buffer)
        return String(buffer, Charset.forName("UTF-8"))
    } catch (e: Exception) {
        return null
    } finally {
        ins?.close()
    }
}
