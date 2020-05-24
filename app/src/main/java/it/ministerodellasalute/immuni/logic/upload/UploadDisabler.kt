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

import java.util.*
import kotlin.math.min
import kotlin.math.pow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class UploadDisabler(
    private val uploadDisablerStore: UploadDisablerStore
) {
    /**
     * At any time shows for how many seconds it upload disabled.
     * Every second the state is updated.
     */
    val disabledForSecondsFlow: Flow<Long?> = flow {
        while (true) {
            emit(disabledForSeconds)
            delay(1000)
        }
    }.distinctUntilChanged().conflate()

    /**
     * Last failed attempt is expired after 24 hours
     */
    private val isLastFailedAttemptExpired: Boolean
        get() {
            val lastFailedAttempt = uploadDisablerStore.lastFailedAttemptTime ?: return false
            val expiredAt = Calendar.getInstance().run {
                time = lastFailedAttempt
                add(Calendar.DATE, 1)
                time
            }
            val currentTime = Date()
            // This might happen if user has set time in forward failed attempt and then has set time back again.
            // In this case let consider time as expired. Otherwise user might be blocked until current time passes
            // future time.
            if (currentTime.before(lastFailedAttempt)) return true
            return expiredAt.before(currentTime)
        }

    /**
     * Returns for how many seconds it's disabled.
     * Note that this value is updated every seconds, therefore this represents current value
     */
    private val disabledForSeconds: Long?
        get() {
            val lastFailedAttempt = uploadDisablerStore.lastFailedAttemptTime ?: return null
            val numFailedAttempts = uploadDisablerStore.numFailedAttempts ?: return null
            if (isLastFailedAttemptExpired) return null

            val secondsToWait =
                min(2.0.pow(numFailedAttempts - 1).toInt() * 5,
                    maxWaitingTimeSeconds
                )
            val secondsSinceLastFailedAttempt = (Date().time - lastFailedAttempt.time) / 1000
            // Already waited enough
            if (secondsSinceLastFailedAttempt >= secondsToWait) return null
            return secondsToWait - secondsSinceLastFailedAttempt
        }

    /**
     * If user successfully uploaded data, we reset disabling state
     */
    fun reset() {
        uploadDisablerStore.lastFailedAttemptTime = null
        uploadDisablerStore.numFailedAttempts = null
    }

    /**
     * User had another failed attempt
     */
    fun submitFailedAttempt() {
        // Reset first if last attempt was expired (so that we don't increment failed attempts of
        // expired attempts.
        if (isLastFailedAttemptExpired) reset()
        uploadDisablerStore.lastFailedAttemptTime = Date()
        uploadDisablerStore.numFailedAttempts =
            (uploadDisablerStore.numFailedAttempts ?: 0) + 1
    }

    companion object {
        // 30 minutes
        const val maxWaitingTimeSeconds = 30 * 60
    }
}
