package org.immuni.android.managers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureInformation
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.tasks.await
import org.immuni.android.extensions.lifecycle.AppLifecycleEvent
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.nearby.ExposureNotificationClientWrapper
import org.immuni.android.util.log
import org.koin.core.KoinComponent
import org.koin.core.inject

class ExposureNotificationManager(val context: Context) : KoinComponent {
    companion object {
        const val REQUEST_CODE_START_EXPOSURE_NOTIFICATION = 620
    }

    val areExposureNotificationsEnabled = ConflatedBroadcastChannel(false)
    val exposureDetails = ConflatedBroadcastChannel<List<ExposureInformation>>(listOf())

    private val exposureNotificationClient: ExposureNotificationClientWrapper by inject()
    private val lifecycleObserver: AppLifecycleObserver by inject()
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    init {
        scope.launch {
            lifecycleObserver.asFlow().filter { it == AppLifecycleEvent.ON_RESUME }.collect {
                update()
            }
        }
    }

    fun cancel() {
        job.cancel()
    }

    private suspend fun update() {
        var isEnabled = false
        var details = listOf<ExposureInformation>()
        try {
            isEnabled = exposureNotificationClient.isEnabled.await()
            if (isEnabled) {
                details = exposureNotificationClient.exposureInformation.await()
            }
        } catch (e: Exception) {
            //FIXME
        }
        areExposureNotificationsEnabled.send(isEnabled)
        exposureDetails.send(details)
    }

    private var optInCompleter: CompletableDeferred<Unit>? = null
    suspend fun optInAndStartExposureTracing(activity: Activity) {
        if (areExposureNotificationsEnabled.value) {
            log("Already enabled. Skipping.")
            return
        }

        try {
            // FIXME
            val exposureConfiguration = ExposureConfiguration.ExposureConfigurationBuilder().apply {
                setMinimumRiskScore(4)
                setAttenuationScores(4, 4, 4, 4, 4, 4, 4, 4)
                setAttenuationWeight(50)
                setDaysSinceLastExposureScores(4, 4, 4, 4, 4, 4, 4, 4)
                setDaysSinceLastExposureWeight(50)
                setDurationScores(4, 4, 4, 4, 4, 4, 4, 4)
                setDurationWeight(50)
                setTransmissionRiskScores(4, 4, 4, 4, 4, 4, 4, 4)
                setTransmissionRiskWeight(50)
            }.build()
            exposureNotificationClient.start(exposureConfiguration).await()
            update()
        } catch (exception: Exception) {
            val completer = optInCompleter
            if (completer != null) {
                log("Error already tried to resolve")
                completer.completeExceptionally(exception)
                return
            }

            if (exception !is ApiException) {
                log("Unknown error")
                throw exception
            }

            if (exception.statusCode == ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
                optInCompleter = completer
                try {
                    exception.status.startResolutionForResult(
                        activity,
                        REQUEST_CODE_START_EXPOSURE_NOTIFICATION
                    )
                    optInCompleter?.await()
                } catch (e: IntentSender.SendIntentException) {
                    log("Error calling startResolutionForResult, sending to settings")
                    optInCompleter?.completeExceptionally(e)
                    return
                } finally {
                    optInCompleter = null
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
        if (requestCode != REQUEST_CODE_START_EXPOSURE_NOTIFICATION) {
            return
        }
        scope.launch {
            if (resultCode == Activity.RESULT_OK) {
                optInCompleter?.complete(Unit)
            } else {
                optInCompleter?.completeExceptionally(Exception("Unknown Exception"))
            }
        }
    }
}
