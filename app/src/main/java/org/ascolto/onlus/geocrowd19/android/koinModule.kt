package org.ascolto.onlus.geocrowd19.android

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.bendingspoons.base.storage.KVStorage
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoMe
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.api.oracle.repository.OracleRepository
import org.ascolto.onlus.geocrowd19.android.api.oracle.repository.OracleRepositoryImpl
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
import org.ascolto.onlus.geocrowd19.android.managers.GeolocationManager
import org.ascolto.onlus.geocrowd19.android.ui.home.HomeSharedViewModel
import org.ascolto.onlus.geocrowd19.android.ui.log.LogViewModel
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.Onboarding
import org.ascolto.onlus.geocrowd19.android.ui.onboarding.OnboardingViewModel
import org.ascolto.onlus.geocrowd19.android.ui.setup.Setup
import org.ascolto.onlus.geocrowd19.android.ui.setup.SetupRepository
import org.ascolto.onlus.geocrowd19.android.ui.setup.SetupRepositoryImpl
import org.ascolto.onlus.geocrowd19.android.ui.setup.SetupViewModel
import org.ascolto.onlus.geocrowd19.android.ui.welcome.Welcome
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.sesame.Sesame
import com.bendingspoons.theirs.Theirs
import com.geouniq.android.GeoUniq
import org.ascolto.onlus.geocrowd19.android.api.oracle.ApiManager
import org.ascolto.onlus.geocrowd19.android.managers.SurveyManager
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

    // single instance of GeoUniq
    single {
        GeoUniq.getInstance(androidContext());
    }

    // single instance of GeolocationManager
    single {
        GeolocationManager(androidContext())
    }

    // single instance of SurveyManager
    single {
        SurveyManager(androidContext())
    }

    // SetupViewModel
    viewModel { SetupViewModel(get()) }

    // HomeSharedViewModel
    viewModel { HomeSharedViewModel(get()) }

    // OnboardingViewModel
    viewModel { (handle: SavedStateHandle) -> OnboardingViewModel(handle, get()) }

    // LogViewModel
    viewModel { (handle: SavedStateHandle) -> LogViewModel(handle, get()) }
}
