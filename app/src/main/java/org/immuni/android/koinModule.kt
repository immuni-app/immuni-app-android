package org.immuni.android

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.security.crypto.MasterKeys
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.network.Network
import org.immuni.android.debugmenu.DebugMenu
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.immuni.android.api.API
import org.immuni.android.data.SettingsDataSource
import org.immuni.android.api.TODOAPIRepository
import org.immuni.android.data.SettingsStore
import org.immuni.android.db.DATABASE_NAME
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.*
import org.immuni.android.config.*
import org.immuni.android.data.SettingsRepository
import org.immuni.android.extensions.lifecycle.AppLifecycleObserver
import org.immuni.android.fcm.FirebaseFCM
import org.immuni.android.ui.forceupdate.ForceUpdateViewModel
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.immuni.android.ui.setup.SetupViewModel
import org.immuni.android.ui.uploaddata.UploadDataViewModel
import org.immuni.android.util.CoroutineContextProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Dependency Injection Koin module.
 */
val appModule = module {

    /**
     * KVStorage to store generic non-database data encrypted using AES256.
     */
    single { KVStorage("state", androidContext(), encrypted = true) }

    /**
     * Room database encrypted using AES256.
     */
    single {
        val key = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val passphrase: ByteArray = SQLiteDatabase.getBytes(key.toCharArray())
        val factory = SupportFactory(passphrase)

        Room.databaseBuilder(
            androidContext(),
            ImmuniDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .openHelperFactory(factory)
            .build()
    }

    /**
     * Backend Retrofit APIs
     */
    single<API> {
        val network: Network by inject()
        network.createServiceAPI(API::class)
    }

    /**
     * Network module.
     */
    single {
        Network(
            ImmuniNetworkConfiguration(androidContext())
        )
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

    /**
     * Firebase FCM.
     */
    single {
        FirebaseFCM(
            androidContext(),
            ImmuniFirebaseFCMConfiguration()
        )
    }

    /**
     * Coroutines contexts provider.
     */
    single { CoroutineContextProvider() }

    /**
     * App lifecycle observer.
     */
    single {
        AppLifecycleObserver()
    }

    single {
        SettingsStore(get())
    }

    single {
        TODOAPIRepository(get())
    }

    single {
        SettingsRepository(get(), get())
    }

    /**
     * Settings Data Source.
     */
    single {
        SettingsDataSource(get(), get(), get())
    }

    single {
        PermissionsManager(androidContext())
    }

    single {
        BluetoothManager(androidContext())
    }

    single {
        UserManager(get())
    }

    single {
        SurveyNotificationManager(androidContext())
    }

    single {
        AppNotificationManager(androidContext())
    }

    single {
        ForceUpdateManager(get(), get(), get())
    }

    // Android ViewModels

    viewModel { SetupViewModel(get(), get()) }
    viewModel { HomeSharedViewModel(get(), get()) }
    viewModel { (handle: SavedStateHandle) -> OnboardingViewModel(handle, get(), get(), get()) }
    viewModel { (userId: String) -> UploadDataViewModel(userId, get(), get()) }
    viewModel { ForceUpdateViewModel(get()) }
}
