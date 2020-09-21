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

package it.ministerodellasalute.immuni.extensions.nearby

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import it.ministerodellasalute.immuni.extensions.bluetooth.BluetoothStateFlow
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.extensions.location.LocationStateFlow
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient.*
import it.ministerodellasalute.immuni.extensions.utils.log
import java.io.File
import java.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ExposureNotificationManager(
    private val locationStateFlow: LocationStateFlow,
    val bluetoothStateFlow: BluetoothStateFlow,
    private val lifecycleObserver: AppLifecycleObserver,
    private val exposureNotificationClient: ExposureNotificationClient
) {
    interface Delegate {
        suspend fun processKeys(
            serverDate: Date,
            summary: ExposureSummary,
            getInfos: suspend () -> List<ExposureInformation>
        )
    }

    constructor(context: Context, lifecycleObserver: AppLifecycleObserver) : this(
        locationStateFlow = LocationStateFlow(context),
        bluetoothStateFlow = BluetoothStateFlow(context),
        lifecycleObserver = lifecycleObserver,
        exposureNotificationClient = ExposureNotificationClientWrapper(
            Nearby.getExposureNotificationClient(
                context
            )
        )
    )

    companion object {
        const val DAYS_OF_SELF_ISOLATION = 14
        const val REQUEST_CODE_START_EXPOSURE_NOTIFICATION = 620
        const val REQUEST_CODE_TEK_HISTORY = 621
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    // Google's Exposure Notification Service is enabled
    private val _areExposureNotificationsEnabled = MutableStateFlow<Boolean?>(null)
    val areExposureNotificationsEnabled: StateFlow<Boolean?> = _areExposureNotificationsEnabled

    // This implies the service is active together with BLE and Location services
    private val _isBroadcastingActive = MutableStateFlow<Boolean?>(null)
    val isBroadcastingActive: StateFlow<Boolean?> = _isBroadcastingActive

    fun deviceSupportsLocationlessScanning() = exposureNotificationClient.deviceSupportsLocationlessScanning()

    private lateinit var delegate: Delegate

    fun setup(delegate: Delegate) {
        this.delegate = delegate

        combine(
            locationStateFlow,
            bluetoothStateFlow,
            areExposureNotificationsEnabled
        ) { isLocationActive, isBluetoothActive, areExposureNotificationsEnabled ->
            log("isLocationActive $isLocationActive")
            log("isBluetoothActive $isBluetoothActive")
            log("areExposureNotificationsEnabled $areExposureNotificationsEnabled")
            when {
                areExposureNotificationsEnabled == null -> null
                // EN on Android 11 don't require active location
                deviceSupportsLocationlessScanning() -> isBluetoothActive && (areExposureNotificationsEnabled == true)
                else -> isLocationActive && isBluetoothActive && (areExposureNotificationsEnabled == true)
            }
        }.conflate().onEach {
            _isBroadcastingActive.value = it
        }.launchIn(scope)

        lifecycleObserver.isActive.filter { it }.onEach {
            update()
        }.launchIn(scope)
    }

    fun cancel() {
        job.cancel()
    }

    suspend fun processKeys(token: String, serverDate: Date) {
        val summary = exposureNotificationClient.getExposureSummary(token)

        delegate.processKeys(serverDate, summary) {
            exposureNotificationClient.getExposureInformation(token)
        }
    }

    suspend fun update() {
        var isEnabled = false
        try {
            isEnabled = exposureNotificationClient.isEnabled()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _areExposureNotificationsEnabled.value = isEnabled
        }
    }

    private var optInCompleter: CompletableDeferred<Unit>? = null
    suspend fun optInAndStartExposureTracing(activity: Activity) {
        if (optInCompleter != null) {
            log("we are already performing this operation")
            return
        }
        if (exposureNotificationClient.isEnabled()) {
            log("Already enabled. Skipping.")
            return
        }

        try {
            exposureNotificationClient.start()
        } catch (exception: Exception) {

            val completer = optInCompleter
            if (completer != null) {
                log("Error already tried to resolve")
                completer.completeExceptionally(exception)
                optInCompleter = null
                return
            }

            if (exception !is ApiException) {
                log("Unknown error")
                throw exception
            }

            if (exception.statusCode == ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
                optInCompleter = CompletableDeferred()
                try {
                    exception.status.startResolutionForResult(
                        activity,
                        REQUEST_CODE_START_EXPOSURE_NOTIFICATION
                    )
                    optInCompleter?.await()
                    optInCompleter = null

                    optInAndStartExposureTracing(activity)
                    return
                } catch (e: IntentSender.SendIntentException) {
                    log("Error calling startResolutionForResult, sending to settings")
                    optInCompleter?.completeExceptionally(e)
                    optInCompleter = null
                    return
                } catch (e: Exception) {
                    log("user denied permissions")
                    optInCompleter = null
                }
            } else {
                log("No RESOLUTION_REQUIRED in result, sending to settings")
                throw exception
            }
        }
        update()
    }

    private var tekRequestCompleter: CompletableDeferred<Unit>? = null
    suspend fun requestTekHistory(activity: Activity): List<TemporaryExposureKey> {
        if (tekRequestCompleter != null) {
            log("we are already performing this operation")
            throw Exception()
        }

        try {
            return exposureNotificationClient.getTemporaryExposureKeyHistory()
        } catch (exception: Exception) {
            val completer = tekRequestCompleter
            if (completer != null) {
                log("Error already tried to resolve")
                completer.completeExceptionally(exception)
                tekRequestCompleter = null
                throw exception
            }

            if (exception !is ApiException) {
                log("Unknown error")
                throw exception
            }

            if (exception.statusCode == ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
                tekRequestCompleter = CompletableDeferred()
                try {
                    exception.status.startResolutionForResult(
                        activity,
                        REQUEST_CODE_TEK_HISTORY
                    )
                    tekRequestCompleter?.await()
                    tekRequestCompleter = null

                    return requestTekHistory(activity)
                } catch (e: IntentSender.SendIntentException) {
                    log("Error calling startResolutionForResult, sending to settings")
                    tekRequestCompleter?.completeExceptionally(e)
                    tekRequestCompleter = null
                    throw e
                } catch (e: Exception) {
                    log("user denied permissions")
                    tekRequestCompleter = null
                    throw e
                }
            } else {
                log("No RESOLUTION_REQUIRED in result, sending to settings")
                throw exception
            }
        }
    }

    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val completer = when (requestCode) {
            REQUEST_CODE_START_EXPOSURE_NOTIFICATION -> optInCompleter
            REQUEST_CODE_TEK_HISTORY -> tekRequestCompleter
            else -> return
        }
        scope.launch {
            if (resultCode == Activity.RESULT_OK) {
                completer?.complete(Unit)
            } else {
                completer?.completeExceptionally(Exception("Unknown Exception"))
            }
        }
    }

    suspend fun stopExposureNotification() {
        // if already disabled, avoid throwing errors
        if (exposureNotificationClient.isEnabled()) {
            exposureNotificationClient.stop()
            update()
        }
    }

    suspend fun provideDiagnosisKeys(
        keyFiles: List<File>,
        configuration: ExposureConfiguration,
        token: String
    ) {
        exposureNotificationClient.provideDiagnosisKeys(
            keyFiles = keyFiles,
            configuration = configuration,
            token = token
        )
    }
}
