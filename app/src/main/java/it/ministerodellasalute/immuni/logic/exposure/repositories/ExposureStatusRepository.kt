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

package it.ministerodellasalute.immuni.logic.exposure.repositories

import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class ExposureStatusRepository(private val storage: KVStorage) {
    companion object {
        val exposureStatusKey = KVStorage.Key<ExposureStatus>("ExposureStatus")
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val _storageExposureStatus = storage.stateFlow(exposureStatusKey, ExposureStatus.None())
    private val _mockExposureStatus = MutableStateFlow<ExposureStatus?>(null)
    var mockExposureStatus: ExposureStatus?
        get() {
            error("Not meant to be called")
        }
        set(value) {
            _mockExposureStatus.value = value
        }

    private val _exposureStatus = MutableStateFlow<ExposureStatus>(
        ExposureStatus.None()
    )
    var exposureStatus: StateFlow<ExposureStatus> = _exposureStatus

    init {
        combine(_storageExposureStatus, _mockExposureStatus) { real, mock ->
            mock ?: real
        }.onEach {
            _exposureStatus.value = it
        }.launchIn(scope)
    }

    fun cancel() {
        job.cancel()
    }

    fun setExposureStatus(status: ExposureStatus) {
        storage[exposureStatusKey] = status
    }

    fun resetExposureStatus() {
        storage.delete(exposureStatusKey)
    }
}
