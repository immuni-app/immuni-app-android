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

package it.ministerodellasalute.immuni.testutils

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

class MockSharedPreferences : SharedPreferences {
    private val preferenceMap: MutableMap<String, Any?> = mutableMapOf()
    private val preferenceEditor: MockSharedPreferenceEditor

    override fun edit(): SharedPreferences.Editor {
        return preferenceEditor
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferenceMap[key] as? Long ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferenceMap[key] as? Float ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return preferenceMap[key] as? MutableSet<String> ?: defValues
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferenceMap[key] as? String ?: defValue
    }

    override fun contains(key: String): Boolean {
        return preferenceMap.contains(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferenceMap[key] as? Boolean ?: defValue
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferenceMap[key] as? Int ?: defValue
    }

    override fun getAll(): MutableMap<String, *> {
        return preferenceMap
    }

    override fun registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener) {}
    override fun unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener) {}

    class MockSharedPreferenceEditor(private val preferenceMap: MutableMap<String, Any?>) :
        SharedPreferences.Editor {

        override fun remove(s: String): SharedPreferences.Editor {
            preferenceMap.remove(s)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            preferenceMap[key] = value
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            preferenceMap[key] = values
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            preferenceMap.clear()
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            preferenceMap[key] = value
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            preferenceMap[key] = value
            return this
        }

        override fun commit(): Boolean {
            return true
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            preferenceMap[key] = value
            return this
        }

        override fun apply() {
            // Nothing to do, everything is saved in memory.
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            preferenceMap[key] = value
            return this
        }
    }

    init {
        preferenceEditor = MockSharedPreferenceEditor(preferenceMap)
    }
}
