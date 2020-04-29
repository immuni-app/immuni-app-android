package org.immuni.android

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.security.crypto.MasterKeys
import org.immuni.android.extensions.storage.KVStorage
import org.immuni.android.ids.Ids
import org.immuni.android.networking.Networking
import org.immuni.android.debugmenu.DebugMenu
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.immuni.android.api.ImmuniAPIRepository
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.bluetooth.BLEAdvertiser
import org.immuni.android.bluetooth.BLEScanner
import org.immuni.android.db.DATABASE_NAME
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.*
import org.immuni.android.bluetooth.ProximityEventsAggregator
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

val appModule = module {

    single { KVStorage("state", androidContext(), encrypted = true) }

    // single instance of ImmuniDatabase
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

    // single CoroutineContextProvider
    single { CoroutineContextProvider() }

    // single instance of Setup
    single { Setup() }

    // single instance of Onboarding
    single { Onboarding() }

    // single instance of Welcome
    single { Welcome() }

    // Ids
    single {
        Ids(androidContext(), encryptIds = true)
    }

    // Networking
    single {
        Networking<ImmuniSettings, ImmuniMe>(
            androidContext(),
            ImmuniNetworkingConfiguration(androidContext())
        )
    }

    // Debug Menu
    single {
        DebugMenu(
            androidContext() as Application,
            ImmuniDebugMenuConfiguration(
                androidContext()
            ), get()
        )
    }

    // Firebase FCM
    single {
        FirebaseFCM(
            androidContext(),
            ImmuniFirebaseFCMConfiguration()
        )
    }

    // single instance of ApiManager
    single {
        ImmuniAPIRepository(get())
    }

    // single instance of BleAdvertiser
    single {
        BLEAdvertiser(androidContext())
    }

    // single instance of BleScanner
    single {
        BLEScanner()
    }

    // single instance of ProximityEventsAggregator
    single {
        ProximityEventsAggregator(get(), get(), 10 * 1000L)
    }

    // single instance of GeolocationManager
    single {
        PermissionsManager(androidContext())
    }

    // single instance of BluetoothManager
    single {
        BluetoothManager(androidContext())
    }

    // single instance of UserManager
    single {
        UserManager()
    }

    // single instance of SurveyManager
    single {
        SurveyManager(get())
    }

    // single instance of AscoltoNotificationManager
    single {
        SurveyNotificationManager(androidContext())
    }

    // single instance of NotificationManager
    single {
        AppNotificationManager(androidContext())
    }

    single {
        BtIdsManager(androidContext())
    }

    // SetupViewModel
    viewModel { SetupViewModel(get(), get(), get(), get(), get()) }

    // HomeSharedViewModel
    viewModel { HomeSharedViewModel(get()) }

    // OnboardingViewModel
    viewModel { (handle: SavedStateHandle) -> OnboardingViewModel(handle, get()) }

    // AddRelativeViewModel
    viewModel { (handle: SavedStateHandle) -> AddRelativeViewModel(handle, get()) }

    // LogViewModel
    viewModel { (handle: SavedStateHandle) -> LogViewModel(handle) }

    // UserDetailsViewModel
    viewModel { (userId: String) -> UserDetailsViewModel(userId) }

    // UploadDataViewModel
    viewModel { (userId: String) -> UploadDataViewModel(userId, get()) }

    // EditDetailsViewModel
    viewModel { (userId: String) -> EditDetailsViewModel(userId) }

    // BleEncountersDebugViewModel
    viewModel { BleEncountersDebugViewModel() }

    // ForceUpdateViewModel
    viewModel { ForceUpdateViewModel() }

}
