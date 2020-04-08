package com.bendingspoons.base.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bendingspoons.base.utils.fromJson
import com.bendingspoons.base.utils.toJson
import java.io.IOException

class KVStorage(
    val name: String,
    private val context: Context,
    val cacheInMemory: Boolean = true,
    val encrypted: Boolean
) {
    val _cache: MutableMap<String, Any> = mutableMapOf()
    val _liveData: MutableMap<String, MutableLiveData<out Any>> = mutableMapOf()

    fun contains(flag: String): Boolean = _cache.contains(flag) || sharedPrefs.contains(flag)

    fun delete(flag: String) {
        _cache.remove(flag)
        sharedPrefs.edit {
            remove(flag)
        }
    }

    inline fun <reified T : Any> save(flag: String, obj: T) {
        if (cacheInMemory) {
            _cache[flag] = obj
        }

        sharedPrefs.edit {
            when (obj) {
                is Boolean -> putBoolean(flag, obj as Boolean)
                is Int -> putInt(flag, obj as Int)
                is Long -> putLong(flag, obj as Long)
                is Float -> putFloat(flag, obj as Float)
                is String -> putString(flag, obj as String)
                else -> {
                    putString(flag, toJson(obj))
                }
            }
        }

        (_liveData[flag] as? MutableLiveData<T>)?.postValue(obj)
    }

    inline fun <reified T : Any> load(flag: String): T? {
        if (!contains(flag)) {
            return null
        }

        if (cacheInMemory) {
            val value = _cache[flag] as? T
            if (value != null) {
                return value
            }
        }

        val getObject: () -> T? = {
            try {
                sharedPrefs.getString(flag, "")?.let {
                    fromJson(it)
                }
            } catch (e: IOException) {
                null
            }
        }

        val value = when (T::class) {
            Boolean::class -> sharedPrefs.getBoolean(flag, false) as T
            Int::class -> sharedPrefs.getInt(flag, 0) as T
            Long::class -> sharedPrefs.getLong(flag, 0) as T
            Float::class -> sharedPrefs.getFloat(flag, 0f) as T
            String::class -> sharedPrefs.getString(flag, "") as T
            else -> getObject()
        }

        if (cacheInMemory) {
            value?.let {
                _cache[flag] = it
            }
        }

        return value
    }

    inline fun <reified T : Any> load(flag: String, defValue: T): T {
        return load(flag) ?: defValue
    }

    // TODO: to be tested
    inline fun <reified T : Any> liveData(flag: String): LiveData<T> {
        val value = load<T>(flag)

        var liveData = _liveData[flag] as? MutableLiveData<T>
        if (liveData == null) {
            liveData = MutableLiveData<T>()
            _liveData[flag] = liveData

            if (value != null) {
                liveData.postValue(value)
            }
        }

        return liveData
    }

    fun clear() {
        _cache.clear()
        _liveData.clear()
        sharedPrefs.edit {
            clear()
        }
    }

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
