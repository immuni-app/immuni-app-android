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

package it.ministerodellasalute.immuni.extensions.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.moshi.Moshi
import it.ministerodellasalute.immuni.extensions.utils.fromJson
import it.ministerodellasalute.immuni.extensions.utils.toJson
import java.io.IOException
import java.lang.ref.WeakReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion

/**
 * A Key-Value storage, backed by [SharedPreferences].
 *
 * It serializes and deserializes primitive types as well as Moshi-serializable objects.
 * It optionally holds an in-memory cache to avoid the deserialization cost upon each [load].
 * It supports storing encrypted key-value pairs through [EncryptedSharedPreferences].
 * It exposes data also as [LiveData].
 *
 * @property name the name of the desired preferences file.
 * @property context the context.
 * @property cacheInMemory whether to cache each non-serialized key-value pair in memory.
 * Defaults to true.
 * @property encrypted whether to serialize an encrypted version of each key-value pair.
 * When encrypted is true, [EncryptedSharedPreferences] are used as the [SharedPreferences]
 * implementation, while when encrypted is false, SharedPreferences are obtained from the passed-in
 * context.
 * @property moshi the instance of [Moshi] to use to serialize and deserialize objects.
 */
class KVStorage(
    val name: String,
    context: Context?,
    val cacheInMemory: Boolean = true,
    val encrypted: Boolean = true,
    val moshi: Moshi,
    // The following two properties should be private but are not, because they're used in public
    // inline methods, thus the underscore prefix.
    val _sharedPrefs: SharedPreferences = getSharedPreferences(context!!, name, encrypted),
    val _cache: MutableMap<Key<*>, Any> = mutableMapOf()
) {
    data class Key<T : Any>(val name: String)

    // This property should be private but is not, because it's used in public inline methods,
    // thus the underscore prefix.
    val _listeners: MutableMap<Key<*>, (Any?) -> WeakReference<out MutableStateFlow<*>>> =
        mutableMapOf()

    /**
     * Checks if the storage contains the given [key].
     */
    fun <T : Any> contains(key: Key<T>): Boolean = synchronized(this) {
        _cache.contains(key) || _sharedPrefs.contains(key.name)
    }

    fun <T : Any> delete(key: Key<T>) {
        synchronized(this) {
            _cache.remove(key)
            _sharedPrefs.edit {
                remove(key.name)
            }

            _listeners[key]?.invoke(null)
        }
    }

    /**
     * Saves [value] for the given [key]. If value's type is not one of the supported primitive types
     * (Boolean, Int, Long, Float, String), it first serializes value to Json with [Moshi]
     * and then saves it as a String.
     */
    inline operator fun <reified T : Any> set(key: Key<T>, value: T) = synchronized(this) {
        if (cacheInMemory) {
            _cache[key] = value
        }

        val k = key.name
        _sharedPrefs.edit {
            when (value) {
                is Boolean -> putBoolean(k, value as Boolean)
                is Int -> putInt(k, value as Int)
                is Long -> putLong(k, value as Long)
                is Float -> putFloat(k, value as Float)
                is String -> putString(k, value as String)
                else -> {
                    putString(k, moshi.toJson(value))
                }
            }
        }

        _listeners[key]?.invoke(value)
    }

    /**
     * Loads the value mapped to the given key, if present, null otherwise.
     * If the specified type T is not one of the supported primitive types
     * (Boolean, Int, Long, Float, String), it first loads the value as String, and then
     * deserializes value to Json with Moshi and returns it.
     */
    inline operator fun <reified T : Any> get(key: Key<T>): T? = synchronized(this) {
        if (!contains(key)) {
            return null
        }

        if (cacheInMemory) {
            val value = _cache[key] as? T
            if (value != null) {
                return value
            }
        }

        val k = key.name

        val getObject: () -> T? = {
            try {
                _sharedPrefs.getString(k, "")?.let {
                    moshi.fromJson(it)
                }
            } catch (e: IOException) {
                null
            }
        }

        val value = when (T::class) {
            Boolean::class -> _sharedPrefs.getBoolean(k, false) as T
            Int::class -> _sharedPrefs.getInt(k, 0) as T
            Long::class -> _sharedPrefs.getLong(k, 0) as T
            Float::class -> _sharedPrefs.getFloat(k, 0f) as T
            String::class -> _sharedPrefs.getString(k, "") as T
            else -> getObject()
        }

        if (cacheInMemory) {
            value?.let {
                _cache[key] = it
            }
        }

        return value
    }

    /**
     * Loads the value mapped to the given [key], if present, or the provided [defaultValue] otherwise.
     * If the specified type T is not one of the supported primitive types
     * (Boolean, Int, Long, Float, String), it first loads the value as String, and then
     * deserializes value to Json with [Moshi] and returns it.
     */
    inline operator fun <reified T : Any> get(key: Key<T>, defaultValue: T): T {
        return this[key] ?: defaultValue
    }

    inline fun <reified T : Any> _addListener(
        key: Key<T>,
        value: T?,
        defaultValue: T?
    ): (Any?) -> WeakReference<out MutableStateFlow<*>> {
        return _listeners.getOrPut(key, {
            val flow: MutableStateFlow<Any?> = MutableStateFlow(value ?: defaultValue)
            flow.onCompletion {
                synchronized(this) {
                    _listeners.remove(key)
                }
            }
            val flowWeakRef = WeakReference(flow)

            return@getOrPut { v: Any? ->
                synchronized(this) {
                    val f = flowWeakRef.get()
                    if (f == null) {
                        _listeners.remove(key)
                    } else {
                        f.value = v ?: defaultValue
                    }
                    flowWeakRef
                }
            }
        })
    }

    /**
     * Loads the value mapped to the given key, and returns a [StateFlow] emitting new values mapped
     * to such key. Such values are optional as the store may initially not contain an entry for
     * the specified key, and it might have such entry [delete]d at some point.
     */
    inline fun <reified T : Any> stateFlow(key: Key<T>): StateFlow<T?> {
        synchronized(this) {
            val value = this[key]
            val listener = _addListener(key, value, null)
            val flow = listener(value)
            return flow.get() as StateFlow<T?>
        }
    }

    /**
     * Loads the value mapped to the given [key], and returns a [StateFlow] emitting new values mapped
     * to such key. Whenever the store does not contain any entry for the specified key, the flow
     * will emit the specified [defaultValue].
     */
    inline fun <reified T : Any> stateFlow(key: Key<T>, defaultValue: T): StateFlow<T> {
        synchronized(this) {
            val value = this[key]
            val listener = _addListener(key, value, defaultValue)
            val flow = listener(value)
            return flow.get() as StateFlow<T>
        }
    }

    /**
     * Clears the storage.
     */
    fun clear() {
        _cache.clear()
        _listeners.clear()
        _sharedPrefs.edit {
            clear()
        }
    }
}

internal fun getSharedPreferences(
    context: Context,
    name: String,
    encrypted: Boolean
): SharedPreferences {
    return if (encrypted) {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } else {
        context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}
