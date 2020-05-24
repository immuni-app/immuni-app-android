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

package it.ministerodellasalute.immuni.logic.upload

import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import java.util.*

class UploadDisablerStore(
    private val kvStorage: KVStorage
) {
    companion object {
        val lastFailedAttemptTimeKey = KVStorage.Key<Date>("lastFailedAttemptTimeKey")
        val numFailedAttemptsKey = KVStorage.Key<Int>("numFailedAttemptsKey")
    }

    var lastFailedAttemptTime: Date?
        get() = kvStorage[lastFailedAttemptTimeKey]
        set(value) {
            if (value != null) kvStorage[lastFailedAttemptTimeKey] = value
            else kvStorage.delete(lastFailedAttemptTimeKey)
        }

    var numFailedAttempts: Int?
        get() = kvStorage[numFailedAttemptsKey]
        set(value) {
            if (value != null) kvStorage[numFailedAttemptsKey] = value
            else kvStorage.delete(numFailedAttemptsKey)
        }
}
