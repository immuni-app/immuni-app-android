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

package it.ministerodellasalute.immuni.config

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.debugmenu.DebugMenuConfiguration
import it.ministerodellasalute.immuni.debugmenu.DebugMenuItem
import it.ministerodellasalute.immuni.extensions.activity.toast
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.extensions.lifecycle.AppActivityLifecycleCallbacks
import it.ministerodellasalute.immuni.extensions.utils.base64EncodedSha256
import it.ministerodellasalute.immuni.logic.exposure.ExposureAnalyticsManager
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import it.ministerodellasalute.immuni.logic.exposure.repositories.ExposureReportingRepository
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.notifications.NotificationType
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import kotlinx.coroutines.*
import java.util.Calendar
import org.koin.core.KoinComponent
import org.koin.core.inject

class ImmuniDebugMenuConfiguration(
    val context: Context
) : DebugMenuConfiguration, KoinComponent {

    private val exposureManager: ExposureManager by inject()
    private val workerManager: WorkerManager by inject()
    private val exposureReportingRepository: ExposureReportingRepository by inject()
    private val notificationManger: AppNotificationManager by inject()
    private val analyticsManager: ExposureAnalyticsManager by inject()
    private val attestationClient: AttestationClient by inject()

    /**
     * In debug mode enable the debug menu.
     */
    override val isDevelopmentDevice = {
        context.resources.getBoolean(R.bool.development_device)
    }

    override fun debuggingItems(): List<DebugMenuItem> {
        return listOf(
            object : DebugMenuItem("\uD83D\uDCD6 Send Exposure Info/Summary", { _, _ ->
                GlobalScope.launch {
                    delay(2000)

                    val topActivity = AppActivityLifecycleCallbacks.topActivity ?: return@launch

                    val exposureSummaries = exposureReportingRepository.getSummaries()

                    val tekHistory = exposureManager.requestTekHistory(topActivity)

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // only email apps should handle this
                        putExtra(Intent.EXTRA_EMAIL, "")
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                        putExtra(
                            Intent.EXTRA_TEXT,
                            listOf(
                                tekHistory.toString(),
                                exposureSummaries.toString()
                            ).joinToString("\n\n")
                        )
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                    context.startActivity(Intent.createChooser(intent, "Choose an app").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                }
            }) {},
            object : DebugMenuItem("\uD83D\uDD11 KEY cleanup", { _, _ ->
                GlobalScope.launch {
                    exposureManager.debugCleanupDatabase()
                }
            }) {},
            object : DebugMenuItem("\uD83D\uDD11 Download KEYs", { _, _ ->
                workerManager.scheduleDebugDiagnosisKeysRequest()
            }) {},
            object : DebugMenuItem("❌ Stop Exposure Notification", { _, _ ->
                GlobalScope.launch {
                    exposureManager.stopExposureNotification()
                }
            }) {},
            object : DebugMenuItem("\uD83D\uDE37 Reset exposure status", { _, _ ->
                exposureManager.setMockExposureStatus(null)
            }) {},
            object : DebugMenuItem("\uD83D\uDE37 Set exposure status NONE", { _, _ ->
                exposureManager.setMockExposureStatus(ExposureStatus.None())
            }) {},
            object : DebugMenuItem("\uD83D\uDE37 Set exposure status CLOSE", { _, _ ->
                exposureManager.setMockExposureStatus(
                    ExposureStatus.Exposed(
                        Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -5)
                        }.time
                    )
                )
            }) {},
            object : DebugMenuItem("\uD83D\uDE37 Set exposure status POSITIVE", { _, _ ->
                exposureManager.setMockExposureStatus(ExposureStatus.Positive())
            }) {},
            object : DebugMenuItem("\uD83D\uDD14 Trigger Force Update Notification", { _, _ ->
                GlobalScope.launch {
                    delay(2000)
                    notificationManger.triggerNotification(NotificationType.ForcedVersionUpdate)
                }
            }) {},
            object : DebugMenuItem("\uD83D\uDD14 Trigger Service not active Notification", { _, _ ->
                notificationManger.triggerNotification(NotificationType.ServiceNotActive)
            }) {},
            object : DebugMenuItem("\uD83D\uDD14 Trigger Exposure Notification", { _, _ ->
                notificationManger.triggerNotification(NotificationType.RiskReminder)
            }) {},
            object : DebugMenuItem("\uD83D\uDD14 Trigger Onboarding Notification", { _, _ ->
                notificationManger.triggerNotification(NotificationType.OnboardingNotCompleted)
            }) {},
            object : DebugMenuItem("\uD83D\uDD14 Send Dummy Analytics", { _, _ ->
                GlobalScope.launch {
                    val isSuccess = analyticsManager.sendOperationalInfo(
                        summary = null,
                        isDummy = true
                    )
                    withContext(Dispatchers.Main) {
                        toast(
                            context,
                            "Dummy analytics result: ${if (isSuccess) "success" else "failure"}"
                        )
                    }
                }
            }) {},
            object : DebugMenuItem("\uD83D\uDD14 Verify Attestation", { _, _ ->
                GlobalScope.launch {
                    val attestationResult = attestationClient.attest("FOO".base64EncodedSha256())
                    withContext(Dispatchers.Main) {
                        when (attestationResult) {
                            is AttestationClient.Result.Success -> toast(context, "Success")
                            is AttestationClient.Result.Invalid -> toast(context, "Invalid")
                            is AttestationClient.Result.Failure -> toast(
                                context,
                                "Failure: ${attestationResult.error}"
                            )
                        }
                    }
                }
            }) {}
        )
    }
}
