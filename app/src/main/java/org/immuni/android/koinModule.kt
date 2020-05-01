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
import org.immuni.android.api.APIManager
import org.immuni.android.api.APIRepository
import org.immuni.android.api.APIStore
import org.immuni.android.db.DATABASE_NAME
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.*
import org.immuni.android.config.*
import org.immuni.android.fcm.FirebaseFCM
import org.immuni.android.ui.addrelative.AddRelativeViewModel
import org.immuni.android.ui.ble.encounters.BleEncountersDebugViewModel
import org.immuni.android.ui.forceupdate.ForceUpdateViewModel
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.home.family.details.UserDetailsViewModel
import org.immuni.android.ui.home.family.details.edit.EditDetailsViewModel
import org.immuni.android.ui.log.LogViewModel
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.setup.SetupViewModel
import org.immuni.android.ui.uploaddata.UploadDataViewModel
import org.immuni.android.ui.welcome.Welcome
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

    single { Setup(get()) }

    single { Onboarding(get()) }

    single { Welcome(get()) }

    single {
        APIStore(get())
    }

    single {
        APIRepository(get(), get())
    }

    single {
        APIManager( get(), get())
    }

    single {
        PermissionsManager(androidContext())
    }

    single {
        BluetoothManager(androidContext())
    }

    single {
        UserManager()
    }

    single {
        SurveyManager(get())
    }

    single {
        SurveyNotificationManager(androidContext())
    }

    single {
        AppNotificationManager(androidContext())
    }

    single {
        BtIdsManager(androidContext())
    }

    single {
        ForceUpdateManager(get(), get())
    }

    // Android ViewModels

    viewModel { SetupViewModel(get(), get(), get(), get(), get()) }
    viewModel { HomeSharedViewModel(get()) }
    viewModel { (handle: SavedStateHandle) -> OnboardingViewModel(handle, get()) }
    viewModel { (handle: SavedStateHandle) -> AddRelativeViewModel(handle, get()) }
    viewModel { (handle: SavedStateHandle) -> LogViewModel(handle) }
    viewModel { (userId: String) -> UserDetailsViewModel(userId) }
    viewModel { (userId: String) -> UploadDataViewModel(userId, get()) }
    viewModel { (userId: String) -> EditDetailsViewModel(userId) }
    viewModel { BleEncountersDebugViewModel() }
    viewModel { ForceUpdateViewModel() }
}
