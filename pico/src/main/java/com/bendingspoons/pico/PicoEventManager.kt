package com.bendingspoons.pico

import android.content.Context
import com.bendingspoons.base.utils.DeviceInfoProvider
import com.bendingspoons.base.utils.DeviceUtils
import com.bendingspoons.pico.model.*
import com.bendingspoons.pico.session.PicoSessionManager
import kotlinx.coroutines.CompletableDeferred

// PicoStore store and load events from the underlying Room database.

internal class PicoEventManager(
    private val context: Context,
    private val config: PicoConfiguration,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val store: PicoStore,
    private val userInfoProviders: Set<PicoUserInfoProvider>,
    private val installInfo: PicoInstallInfo,
    private val sessionManager: CompletableDeferred<PicoSessionManager>
) {
    suspend fun trackEvent(trackEvent: TrackEvent) {
        val picoEvent = picoEvent(addSessionData(trackEvent))
        store.store(picoEvent)
    }

    private suspend fun addSessionData(trackEvent: TrackEvent): TrackEvent {
        return when (trackEvent) {
            is UserAction -> {
                val updatedInfo = mutableMapOf<String, Any?>().apply {
                    putAll(trackEvent.info)
                    put("session_id", sessionManager.await().getSessionId())
                    put("last_foreground_session_id", sessionManager.await().getLastForegroundSessionId())
                    put("seconds_from_session_start", sessionManager.await().getSecondsFromSessionStart())
                    put("is_background_event", sessionManager.await().isInBackground())
                }
                return trackEvent.copy(info = updatedInfo)
            }
            else -> trackEvent
        }
    }

    private fun picoEvent(event: TrackEvent): PicoEvent {
        val settings = config.oracle().settings()

        return PicoEvent(
            id = event.id,
            timestamp = System.currentTimeMillis() / 1000.0,
            requestTimestamp = 0.0, // filled by the PicoDispatcher during actual sending
            app = DeviceUtils.appPackage(context),
            type = event.type.name,
            data = event.data,
            user = PicoUser(
                ids = config.concierge().allIds().map { Pair(it.name, it.id) }.toMap().toMutableMap(),
                info = PicoBaseUserInfo(
                    country = deviceInfoProvider.country(),
                    language = deviceInfoProvider.language(),
                    appLanguage = deviceInfoProvider.appLanguage(),
                    locale = deviceInfoProvider.locale(),
                    appVersion = DeviceUtils.appVersionName(context),
                    bundleVersion = DeviceUtils.appVersionCode(context).toString(),
                    firstInstallTime = installInfo.firstInstallDate.let { it.time / 1000.0 } ?: 0.0,
                    lastInstallTime = installInfo.lastInstallDate.let { it.time / 1000.0 } ?: 0.0,
                    installedBeforePico = installInfo.wasInstalledBeforePico,
                    isBaseline = settings?.isBaseline ?: false,
                    isFree = settings?.isFree ?: false,
                    timezone = TimezoneInfo(
                        seconds = deviceInfoProvider.timeZoneSecons(),
                        name = deviceInfoProvider.timeZoneName(),
                        daylightSaving = deviceInfoProvider.isDayLightSaving()
                    ),
                    monetization = MonetizationInfo(
                        isSubscribed = config.oracle().me()?.isSubscribed ?: false,
                        customFields = config.additionalMonetizationInfo(),
                        availableProductIds = settings?.availableProductsId() ?: listOf()
                    ),
                    experiment = settings?.experimentsSegments ?: mapOf(),
                    device = DeviceInfo(
                        androidVersion = deviceInfoProvider.androidVersion(),
                        screenSize = deviceInfoProvider.screenSize(context),
                        platform = deviceInfoProvider.devicePlatform()
                    )
                ),
                additionalInfo = additionalUserInfo()
            )
        )
    }

    private fun additionalUserInfo(): Map<String, Any> {
        return userInfoProviders.fold(mutableMapOf()) { map, provider ->
            map.apply { putAll(provider.userInfo) }
        }
    }
}
