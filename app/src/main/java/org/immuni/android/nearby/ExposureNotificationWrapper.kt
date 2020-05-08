package org.immuni.android.nearby

import com.google.android.gms.nearby.exposurenotification.*
import com.google.android.gms.nearby.exposurenotification.ExposureInformation.ExposureInformationBuilder
import com.google.android.gms.nearby.exposurenotification.ExposureSummary.ExposureSummaryBuilder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import org.koin.core.KoinComponent

/**
 * Wrapper around [com.google.android.gms.nearby.Nearby] APIs.
 */
class ExposureNotificationClientWrapper(private val client: ExposureNotificationClient) :
    KoinComponent {
    companion object {
        private const val SHOULD_PROVIDE_FAKES = true
    }

    fun start(configuration: ExposureConfiguration): Task<Void> {
        return client.start(configuration)
    }

    fun stop(): Task<Void> {
        return client.stop()
    }

    val isEnabled: Task<Boolean>
        get() = client.isEnabled

    val temporaryExposureKeyHistory: Task<List<TemporaryExposureKey>>
        get() = client.temporaryExposureKeyHistory

    fun provideDiagnosisKeys(keys: List<TemporaryExposureKey>): Task<Void> {
        return client.provideDiagnosisKeys(keys)
    }

    val maxDiagnosisKeysCount: Task<Int>
        get() = client.maxDiagnosisKeyCount

    val exposureSummary: Task<ExposureSummary>
        get() = if (SHOULD_PROVIDE_FAKES) {
            Tasks.forResult(
                ExposureSummaryBuilder()
                    .setMatchedKeyCount(2)
                    .setDaysSinceLastExposure(1)
                    .build()
            )
        } else {
            client.exposureSummary
        }

    val exposureInformation: Task<List<ExposureInformation>>
        get() {
            return if (SHOULD_PROVIDE_FAKES) {
                Tasks.forResult(
                    listOf(
                        ExposureInformationBuilder()
                            .setAttenuationValue(1)
                            .setDateMillisSinceEpoch(System.currentTimeMillis())
                            .setDurationMinutes(5)
                            .build(),
                        ExposureInformationBuilder()
                            .setAttenuationValue(1)
                            .setDateMillisSinceEpoch(1588075162258L)
                            .setDurationMinutes(10)
                            .build()
                    )
                )
            } else {
                client.exposureInformation
            }
        }

    fun resetAllData(): Task<Void> {
        return client.resetAllData()
    }

    fun resetTemporaryExposureKey(): Task<Void> {
        return client.resetTemporaryExposureKey()
    }
}
