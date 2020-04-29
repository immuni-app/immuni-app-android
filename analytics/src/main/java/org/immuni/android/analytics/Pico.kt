package org.immuni.android.analytics

import android.content.Context
import org.immuni.android.extensions.utils.DeviceInfoProviderImpl
import org.immuni.android.analytics.api.PicoRetrofit
import org.immuni.android.analytics.api.PicoService
import org.immuni.android.analytics.db.picoDatabase
import org.immuni.android.analytics.install.PicoInstallManager
import org.immuni.android.analytics.model.TrackEvent
import org.immuni.android.analytics.model.UserAction
import org.immuni.android.analytics.session.PicoSessionManager
import org.immuni.android.analytics.userconsent.UserConsent
import org.immuni.android.analytics.userconsent.UserConsentLevel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Pico, metrics library.
 *
 * This is the Pico lib main class.
 * @param context
 * @param config the [PicoConfiguration] injected by the app.
 */
class Pico(
    private val context: Context,
    private val config: PicoConfiguration
) {

    private val api: PicoService = PicoRetrofit(config).retrofit.create(PicoService::class.java)
    private val store: PicoStore
    private val dispatcher: PicoDispatcher
    private val eventFlow: PicoFlow
    private val installManager: PicoInstallManager
    private val eventManagerCompletable = CompletableDeferred<PicoEventManager>()
    private val sessionManagerCompletable = CompletableDeferred<PicoSessionManager>()
    private lateinit var collector: PicoCollector
    private val userInfoProviders = mutableSetOf<PicoUserInfoProvider>()
    private val userConsent = UserConsent(context, config)

    init {
        // TODO remove
        GlobalScope.launch {
            saveUserConsent(UserConsentLevel.ACCEPTED)
        }

        store = PicoStoreImpl(
            picoDatabase(context),
            userConsent
        )
        dispatcher = PicoDispatcher(api)
        eventFlow = PicoFlow(store, userConsent)
        installManager = PicoInstallManager(context, config, eventManagerCompletable)

        registerUserInfoProvider(config)
    }

    // call this initialization method only after having initialised all the infoProvider
    // in this way the eventManager can use the info provider safely.

    fun setup() {
        val sessionManager = PicoSessionManager(context, eventManagerCompletable, eventFlow)

        val eventManager = PicoEventManager(
            context,
            config,
            DeviceInfoProviderImpl(),
            store,
            userInfoProviders,
            installManager.info,
            sessionManagerCompletable
        )

        collector = PicoCollector(
            eventFlow,
            dispatcher,
            store
        ).apply {
            GlobalScope.launch { start() }
        }

        this.eventManagerCompletable.complete(eventManager)
        this.sessionManagerCompletable.complete(sessionManager)
    }

    fun flush() {
        eventFlow.flush()
    }

    suspend fun trackEvent(trackEvent: TrackEvent) {
        eventManagerCompletable.await().trackEvent(trackEvent)
    }

    suspend fun trackUserAction(id: String, vararg info: Pair<String, Any?>) {
        trackEvent(UserAction(id, info.toMap()))
    }

    private fun saveUserConsent(level: UserConsentLevel) {
        userConsent.level = level
    }

    fun registerUserInfoProvider(provider: PicoUserInfoProvider) {
        assert(!eventManagerCompletable.isCompleted) {
            "registration of UserInfoProviders must be done before the setup"
        }
        userInfoProviders.add(provider)
    }

    companion object {
        const val VERSION: String = "1.0.0"
    }
}
