package org.immuni.android

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.bendingspoons.base.storage.KVStorage
import org.immuni.android.api.oracle.model.AscoltoMe
import org.immuni.android.api.oracle.model.AscoltoSettings
import org.immuni.android.api.oracle.repository.OracleRepository
import org.immuni.android.api.oracle.repository.OracleRepositoryImpl
import org.immuni.android.db.AscoltoDatabase
import org.immuni.android.managers.GeolocationManager
import org.immuni.android.ui.home.HomeSharedViewModel
import org.immuni.android.ui.log.LogViewModel
import org.immuni.android.ui.onboarding.Onboarding
import org.immuni.android.ui.onboarding.OnboardingViewModel
import org.immuni.android.ui.setup.Setup
import org.immuni.android.ui.setup.SetupRepository
import org.immuni.android.ui.setup.SetupRepositoryImpl
import org.immuni.android.ui.setup.SetupViewModel
import org.immuni.android.ui.welcome.Welcome
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.theirs.Theirs
import de.fraunhofer.iis.Estimator
import org.immuni.android.api.oracle.ApiManager
import org.immuni.android.managers.AscoltoNotificationManager
import org.immuni.android.managers.BluetoothManager
import org.immuni.android.managers.SurveyManager
import org.immuni.android.ui.addrelative.AddRelativeViewModel
import org.immuni.android.ui.ble.BleDebugViewModel
import org.immuni.android.ui.home.family.details.UserDetailsViewModel
import org.immuni.android.ui.home.family.details.edit.EditDetailsViewModel
import org.immuni.android.ui.uploadData.UploadDataViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { KVStorage("state", androidContext()) }

    // single instance of TDFDatabase
    single {
        Room.databaseBuilder(
            androidContext(),
            AscoltoDatabase::class.java,
            "ascolto_database"
        )
            .fallbackToDestructiveMigration()
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
        Concierge.Manager(androidContext(), appCustomIdProvider = AscoltoConciergeCustomIdProvider())
    }

    // Oracle - Lib
    single {
        Oracle<AscoltoSettings, AscoltoMe>(androidContext(), AscoltoOracleConfiguration(androidContext()))
    }

    // Secret Menu - Lib
    single {
        SecretMenu(androidContext(), AscoltoSecretMenuConfiguration(androidContext()), get())
    }

    // Theirs - Lib
    single {
        Theirs(androidContext(), AscoltoTheirsConfiguration())
    }

    // Pico - Lib
    single {
        Pico(androidContext(), AscoltoPicoConfiguration(androidContext()))
    }

    // single instance of OracleRepository
    single<OracleRepository> { OracleRepositoryImpl(androidContext(), get(), get()) }

    single {
        ApiManager()
    }

    // single instance of GeolocationManager
    single {
        GeolocationManager(androidContext())
    }

    // single instance of BluetoothManager
    single {
        BluetoothManager(androidContext())
    }

    // single instance of SurveyManager
    single {
        SurveyManager(androidContext())
    }

    // single instance of AscoltoNotificationManager
    single {
        AscoltoNotificationManager(androidContext())
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
    viewModel { (handle: SavedStateHandle) -> LogViewModel(handle, get()) }

    // UserDetailsViewModel
    viewModel { (userId: String) -> UserDetailsViewModel(userId) }

    // UploadDataViewModel
    viewModel { (userId: String) -> UploadDataViewModel(userId, get()) }

    // EditDetailsViewModel
    viewModel { (userId: String) -> EditDetailsViewModel(userId) }

    // BleDebugViewModel
    viewModel { BleDebugViewModel() }

}
