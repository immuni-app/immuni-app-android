package org.immuni.android

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.security.crypto.MasterKeys
import com.bendingspoons.base.storage.KVStorage
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.theirs.Theirs
import de.fraunhofer.iis.Estimator
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.api.oracle.repository.OracleRepository
import org.immuni.android.api.oracle.repository.OracleRepositoryImpl
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.managers.SurveyNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.PermissionsManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.managers.*
import org.immuni.android.ui.addrelative.AddRelativeViewModel
import org.immuni.android.ui.ble.distance.BleDistanceDebugViewModel
import org.immuni.android.ui.ble.encounters.BleEncountersDebugViewModel
import org.immuni.android.ui.force_update.ForceUpdateViewModel
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.home.family.details.UserDetailsViewModel
import org.immuni.android.ui.home.family.details.edit.EditDetailsViewModel
import org.immuni.android.ui.log.LogViewModel
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.setup.SetupRepository
import org.immuni.android.ui.setup.SetupRepositoryImpl
import org.immuni.android.ui.setup.SetupViewModel
import org.immuni.android.ui.uploadData.UploadDataViewModel
import org.immuni.android.ui.welcome.Welcome
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
            "immuni_database"
        )
            .fallbackToDestructiveMigration()
            .openHelperFactory(factory)
            .build()
    }

    // single distance Estimator
    single { Estimator(2L*1000, -90F, 2f) }

    // single instance of Setup
    single { Setup() }

    // single instance of Onboarding
    single { Onboarding() }

    // single instance of Welcome
    single { Welcome() }

    // single instance of SetupRepository
    single<SetupRepository> {
        SetupRepositoryImpl(
            androidContext(),
            get(), get()
        )
    }

    // Concierge - Lib
    single {
        Concierge.Manager(androidContext(), appCustomIdProvider = ImmuniConciergeCustomIdProvider(), encryptIds = true)
    }

    // Oracle - Lib
    single {
        Oracle<ImmuniSettings, ImmuniMe>(androidContext(), ImmuniOracleConfiguration(androidContext()))
    }

    // Secret Menu - Lib
    single {
        SecretMenu(androidContext(), ImmuniSecretMenuConfiguration(androidContext()), get())
    }

    // Theirs - Lib
    single {
        Theirs(androidContext(), ImmuniTheirsConfiguration())
    }

    // Pico - Lib
    single {
        Pico(androidContext(), ImmuniPicoConfiguration(androidContext()))
    }

    // single instance of OracleRepository
    single<OracleRepository> { OracleRepositoryImpl(androidContext(), get(), get()) }

    single {
        ApiManager()
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
        SurveyManager()
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
    viewModel { SetupViewModel(get()) }

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

    // BleDebugViewModel
    viewModel { BleDistanceDebugViewModel() }

    // BleEncountersDebugViewModel
    viewModel { BleEncountersDebugViewModel() }

    // ForceUpdateViewModel
    viewModel { ForceUpdateViewModel() }

}
