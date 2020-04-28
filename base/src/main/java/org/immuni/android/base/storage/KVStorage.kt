package org.immuni.android.base.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import org.immuni.android.base.utils.fromJson
import org.immuni.android.base.utils.toJson
import java.io.IOException

/**
 * A Key-Value storage, backed by [SharedPreferences].
 *
 * It serializes and deserializes primitive types as well as Moshi-serializable objects.
 * It optionally holds an in-memory cache to avoid the deserialization cost upon each [load].
 * It supports storing encrypted key-value pairs through [EncryptedSharedPreferences].
 *
 * @property name the name of the desired preferences file.
 * @property context the context.
 * @property cacheInMemory whether to cache each non-serialized key-value pair in memory.
 * Defaults to true.
 * @property encrypted whether to serialize an encrypted version of each key-value pair.
 * When encrypted is true, [EncryptedSharedPreferences] are used as the [SharedPreferences]
 * implementation, while when encrypted is false, SharedPreferences are obtained from the passed-in
 * context.
 */
class KVStorage(
    val name: String,
    private val context: Context,
    val cacheInMemory: Boolean = true,
    val encrypted: Boolean
) {
    // The following two properties should be private but are not, because they're used in public
    // inline methods, thus the underscore prefix.
    val _cache: MutableMap<String, Any> = mutableMapOf()
    val _liveData: MutableMap<String, MutableLiveData<out Any>> = mutableMapOf()

    /**
     * Checks if the storage contains the given key.
     */
    fun contains(key: String): Boolean = _cache.contains(key) || sharedPrefs.contains(key)

    fun delete(key: String) {
        _cache.remove(key)
        sharedPrefs.edit {
            remove(key)
        }
    }

    /**
     * Saves value for the given key. If value's type is not one of the supported primitive types
     * (Boolean, Int, Long, Float, String), it first serializes value to Json with Moshi
     * and then saves it as a String.
     */
    inline fun <reified T : Any> save(key: String, value: T) {
        if (cacheInMemory) {
            _cache[key] = value
        }

        sharedPrefs.edit {
            when (value) {
                is Boolean -> putBoolean(key, value as Boolean)
                is Int -> putInt(key, value as Int)
                is Long -> putLong(key, value as Long)
                is Float -> putFloat(key, value as Float)
                is String -> putString(key, value as String)
                else -> {
                    putString(key, toJson(value))
                }
            }
        }

        (_liveData[key] as? MutableLiveData<T>)?.postValue(value)
    }

    /**
     * Loads the value mapped to the given key, if present, null otherwise.
     * If the specified type T is not one of the supported primitive types
     * (Boolean, Int, Long, Float, String), it first loads the value as String, and then
     * deserializes value to Json with Moshi and returns it.
     */
    inline fun <reified T : Any> load(key: String): T? {
        if (!contains(key)) {
            return null
        }

        if (cacheInMemory) {
            val value = _cache[key] as? T
            if (value != null) {
                return value
            }
        }

        val getObject: () -> T? = {
            try {
                sharedPrefs.getString(key, "")?.let {
                    fromJson(it)
                }
            } catch (e: IOException) {
                null
            }
        }

        val value = when (T::class) {
            Boolean::class -> sharedPrefs.getBoolean(key, false) as T
            Int::class -> sharedPrefs.getInt(key, 0) as T
            Long::class -> sharedPrefs.getLong(key, 0) as T
            Float::class -> sharedPrefs.getFloat(key, 0f) as T
            String::class -> sharedPrefs.getString(key, "") as T
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
     * Loads the value mapped to the given key, if present, or the provided defaultValue otherwise.
     * If the specified type T is not one of the supported primitive types
     * (Boolean, Int, Long, Float, String), it first loads the value as String, and then
     * deserializes value to Json with Moshi and returns it.
     */
    inline fun <reified T : Any> load(flag: String, defaultValue: T): T {
        return load(flag) ?: defaultValue
    }

    /**
     * Loads the value mapped to the given key, and returns a LiveData subscribed to such key.
     */
    inline fun <reified T : Any> liveData(key: String): LiveData<T> {
        val value = load<T>(key)

        var liveData = _liveData[key] as? MutableLiveData<T>
        if (liveData == null) {
            liveData = MutableLiveData<T>()
            _liveData[key] = liveData

            if (value != null) {
                liveData.postValue(value)
            }
        }

        return liveData
    }

    /**
     * Clears the storage.
     */
    fun clear() {
        _cache.clear()
        _liveData.clear()
        sharedPrefs.edit {
            clear()
        }
    }

    // This should be private but is not, because it's used in public inline methods.
    val sharedPrefs: SharedPreferences
        get() = if (encrypted)
            EncryptedSharedPreferences.create(
                name,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        else
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
}
