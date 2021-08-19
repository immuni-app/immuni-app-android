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

package it.ministerodellasalute.immuni

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dgca.verifier.app.decoder.base45.Base45Service
import dgca.verifier.app.decoder.base45.DefaultBase45Service
import dgca.verifier.app.decoder.cbor.CborService
import dgca.verifier.app.decoder.cbor.DefaultCborService
import dgca.verifier.app.decoder.compression.CompressorService
import dgca.verifier.app.decoder.compression.DefaultCompressorService
import dgca.verifier.app.decoder.cose.CoseService
import dgca.verifier.app.decoder.cose.CryptoService
import dgca.verifier.app.decoder.cose.DefaultCoseService
import dgca.verifier.app.decoder.cose.VerificationCryptoService
import dgca.verifier.app.decoder.prefixvalidation.DefaultPrefixValidationService
import dgca.verifier.app.decoder.prefixvalidation.PrefixValidationService
import dgca.verifier.app.decoder.schema.DefaultSchemaValidator
import dgca.verifier.app.decoder.schema.SchemaValidator
import it.ministerodellasalute.immuni.api.services.*
import it.ministerodellasalute.immuni.config.*
import it.ministerodellasalute.immuni.debugmenu.DebugMenu
import it.ministerodellasalute.immuni.extensions.attestation.AttestationClient
import it.ministerodellasalute.immuni.extensions.attestation.SafetyNetAttestationClient
import it.ministerodellasalute.immuni.extensions.lifecycle.AppActivityLifecycleCallbacks
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager
import it.ministerodellasalute.immuni.extensions.notifications.PushNotificationManager
import it.ministerodellasalute.immuni.extensions.storage.KVStorage
import it.ministerodellasalute.immuni.extensions.utils.moshi
import it.ministerodellasalute.immuni.logic.exposure.BaseOperationalInfo
import it.ministerodellasalute.immuni.logic.exposure.CountriesOfInterestManager
import it.ministerodellasalute.immuni.logic.exposure.ExposureAnalyticsManager
import it.ministerodellasalute.immuni.logic.exposure.ExposureManager
import it.ministerodellasalute.immuni.logic.exposure.models.ExposureStatus
import it.ministerodellasalute.immuni.logic.exposure.repositories.*
import it.ministerodellasalute.immuni.logic.forceupdate.ForceUpdateManager
import it.ministerodellasalute.immuni.logic.greencovidcertificate.DCCManager
import it.ministerodellasalute.immuni.logic.notifications.AppNotificationManager
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsNetworkRepository
import it.ministerodellasalute.immuni.logic.settings.repositories.ConfigurationSettingsStoreRepository
import it.ministerodellasalute.immuni.logic.upload.CunValidator
import it.ministerodellasalute.immuni.logic.upload.OtpGenerator
import it.ministerodellasalute.immuni.logic.upload.UploadDisabler
import it.ministerodellasalute.immuni.logic.upload.UploadDisablerStore
import it.ministerodellasalute.immuni.logic.user.UserManager
import it.ministerodellasalute.immuni.logic.user.repositories.RegionRepository
import it.ministerodellasalute.immuni.logic.user.repositories.UserRepository
import it.ministerodellasalute.immuni.logic.worker.WorkerManager
import it.ministerodellasalute.immuni.network.Network
import it.ministerodellasalute.immuni.ui.cun.CunViewModel
import it.ministerodellasalute.immuni.ui.faq.FaqViewModel
import it.ministerodellasalute.immuni.ui.forceupdate.ForceUpdateViewModel
import it.ministerodellasalute.immuni.ui.greencertificate.GreenCertificateViewModel
import it.ministerodellasalute.immuni.ui.howitworks.HowItWorksDataSource
import it.ministerodellasalute.immuni.ui.main.MainViewModel
import it.ministerodellasalute.immuni.ui.onboarding.OnboardingViewModel
import it.ministerodellasalute.immuni.ui.otp.OtpViewModel
import it.ministerodellasalute.immuni.ui.settings.SettingsViewModel
import it.ministerodellasalute.immuni.ui.setup.SetupViewModel
import it.ministerodellasalute.immuni.ui.suggestions.StateCloseViewModel
import it.ministerodellasalute.immuni.ui.support.SupportViewModel
import it.ministerodellasalute.immuni.ui.upload.UploadViewModel
import it.ministerodellasalute.immuni.util.CoroutineContextProvider
import it.ministerodellasalute.immuni.util.DigitValidator
import it.ministerodellasalute.immuni.workers.models.ServiceNotActiveNotificationWorkerStatus
import it.ministerodellasalute.immuni.workers.repositories.ServiceNotActiveNotificationWorkerRepository
import java.security.SecureRandom
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Dependency Injection Koin module.
 */
val appModule = module {

    single { DefaultPrefixValidationService() as PrefixValidationService }
    single { DefaultBase45Service() as Base45Service }
    single { DefaultCompressorService() as CompressorService }
    single { VerificationCryptoService() as CryptoService }
    single { DefaultCoseService() as CoseService }
    single { DefaultSchemaValidator() as SchemaValidator }
    single { DefaultCborService() as CborService }

    /**
     * App Configuration Service APIs
     */
    single {
        val network = Network(
            androidContext(),
            SettingsNetworkConfiguration(androidContext(), get())
        )
        network.createServiceAPI(ConfigurationSettingsService::class)
    }

    /**
     * Exposure Reporting Service APIs
     */
    single {
        val network = Network(
            androidContext(), ExposureReportingNetworkConfiguration(androidContext(), get())
        )
        network.createServiceAPI(ExposureReportingService::class)
    }

    /**
     * Exposure Ingestion Service APIs
     */
    single {
        val network = Network(
            androidContext(),
            ExposureIngestionNetworkConfiguration(
                androidContext(),
                get(),
                get()
            )
        )
        network.createServiceAPI(ExposureIngestionService::class)
    }

    /**
     * Exposure Analytics Service APIs
     */
    single {
        val network = Network(
            androidContext(), ExposureAnalyticsNetworkConfiguration(androidContext(), get())
        )
        network.createServiceAPI(ExposureAnalyticsService::class)
    }

    /**
     * GDC Service APIs
     */
    single {
        val network = Network(
            androidContext(),
            DigitalCovidCertificateNetworkConfiguration(
                androidContext(),
                get(),
                get()
            )
        )
        network.createServiceAPI(DCCService::class)
    }

    /**
     * Debug Menu module.
     */
    single {
        DebugMenu(
            androidContext() as Application,
            ImmuniDebugMenuConfiguration(
                androidContext()
            )
        )
    }

    single { CoroutineContextProvider() }

    single {
        AppLifecycleObserver()
    }

    single {
        AppActivityLifecycleCallbacks()
    }

    single {
        ConfigurationSettingsStoreRepository(
            androidContext(),
            KVStorage(
                name = "ConfigurationSettingsStoreRepository",
                context = androidContext(),
                encrypted = true,
                moshi = get()
            ),
            defaultSettings
        )
    }

    single {
        RegionRepository()
    }

    single {
        ServiceNotActiveNotificationWorkerRepository(
            KVStorage(
                name = "ServiceNotActiveNotificationWorkerRepository",
                context = androidContext(),
                cacheInMemory = false,
                encrypted = true,
                moshi = immuniMoshi
            )
        )
    }

    single {
        OtpGenerator(SecureRandom())
    }

    factory { (showFaq: Boolean) ->
        HowItWorksDataSource(androidContext(), showFaq)
    }

    single {
        ConfigurationSettingsManager(
            androidContext(),
            get(),
            get()
        )
    }

    single {
        ConfigurationSettingsNetworkRepository(get())
    }

    single {
        UploadDisablerStore(
            KVStorage(
                name = "UploadDisablerStore",
                context = androidContext(),
                encrypted = true,
                moshi = get()
            )
        )
    }

    single {
        UploadDisabler(get())
    }

    single {
        ExposureNotificationManager(androidContext(), get())
    }

    single {
        ExposureManager(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single {
        DCCManager(get())
    }

    single {
        ExposureAnalyticsStoreRepository(
            KVStorage(
                name = "ExposureAnalyticsStoreRepository",
                context = androidContext(),
                moshi = get(),
                cacheInMemory = true,
                encrypted = true
            )
        )
    }

    single {
        ExposureAnalyticsNetworkRepository(get())
    }

    single<AttestationClient> {
        SafetyNetAttestationClient(
            androidContext(),
            SafetyNetAttestationClient.AttestationParameters(
                apiKey = BuildConfig.SAFETY_NET_API_KEY,
                apkPackageName = androidContext().packageName,
                requiresBasicIntegrity = true,
                requiresCtsProfile = true,
                requiresHardwareAttestation = true
            )
        )
    }

    single {
        ExposureAnalyticsManager(
            get<ExposureAnalyticsStoreRepository>(),
            get(),
            get(),
            get(),
            get(),
            { get() }
        )
    }

    single {
        ExposureStatusRepository(
            KVStorage(
                name = "ExposureStatusRepository",
                context = androidContext(),
                moshi = get(),
                cacheInMemory = true,
                encrypted = true
            )
        )
    }

    single {
        ExposureReportingRepository(
            KVStorage(
                name = "ExposureReportingRepository",
                context = androidContext(),
                moshi = get(),
                cacheInMemory = false,
                encrypted = true
            )
        )
    }

    single {
        CunValidator()
    }

    single {
        DigitValidator()
    }

    single {
        ExposureIngestionRepository(get())
    }

    single {
        GCDRepository(get())
    }

    single {
        PushNotificationManager(androidContext())
    }

    single {
        UserManager(get(), get())
    }

    single {
        UserRepository(
            KVStorage(
                name = "UserRepository",
                context = androidContext(),
                encrypted = true,
                moshi = immuniMoshi
            )

        )
    }

    single {
        WorkerManager(androidContext(), get(), get())
    }

    single {
        AppNotificationManager(androidContext())
    }

    single {
        ForceUpdateManager(get(), get())
    }

    single {
        immuniMoshi
    }

    single {
        CountriesOfInterestManager(get(), get())
    }

    factory {
        BaseOperationalInfo(get(), get(), get())
    }

    // Android ViewModels

    viewModel { SetupViewModel(get(), get()) }
    viewModel {
        MainViewModel(
            androidContext(),
            get(),
            get(),
            get()
        )
    }
    viewModel { (handle: SavedStateHandle) ->
        OnboardingViewModel(handle, get(), get(), get(), get())
    }
    viewModel {
        OtpViewModel(get(), get(), get(), get())
    }
    viewModel { UploadViewModel(get()) }
    viewModel { ForceUpdateViewModel(get()) }
    viewModel { FaqViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { StateCloseViewModel(get(), get()) }
    viewModel { SupportViewModel(androidContext(), get(), get()) }
    viewModel { CunViewModel(get(), get(), get()) }
    viewModel { GreenCertificateViewModel(androidContext(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

val immuniMoshi = moshi(
    extraAdapters = mapOf(
        ExposureIngestionService.Province::class to ExposureIngestionService.Province.MoshiAdapter()
    ),
    extraFactories = listOf(
        ExposureStatus.moshiAdapter,
        ServiceNotActiveNotificationWorkerStatus.moshiAdapter
    )
)
