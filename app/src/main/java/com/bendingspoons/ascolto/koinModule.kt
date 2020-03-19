package com.bendingspoons.ascolto

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import com.bendingspoons.ascolto.api.oracle.repository.OracleRepository
import com.bendingspoons.ascolto.api.oracle.repository.OracleRepositoryImpl
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.ascolto.ui.home.HomeSharedViewModel
import com.bendingspoons.ascolto.ui.log.LogViewModel
import com.bendingspoons.ascolto.ui.onboarding.Onboarding
import com.bendingspoons.ascolto.ui.onboarding.OnboardingViewModel
import com.bendingspoons.ascolto.ui.setup.Setup
import com.bendingspoons.ascolto.ui.setup.SetupRepository
import com.bendingspoons.ascolto.ui.setup.SetupRepositoryImpl
import com.bendingspoons.ascolto.ui.setup.SetupViewModel
import com.bendingspoons.ascolto.ui.welcome.Welcome
import com.bendingspoons.concierge.Concierge
import com.bendingspoons.oracle.Oracle
import com.bendingspoons.pico.Pico
import com.bendingspoons.secretmenu.SecretMenu
import com.bendingspoons.sesame.Sesame
import com.bendingspoons.theirs.Theirs
import com.bendingspoons.thirtydayfitness.AscoltoConciergeCustomIdProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

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
        Oracle(androidContext(), AscoltoOracleConfiguration(androidContext()))
    }

    // Secret Menu - Lib
    single {
        SecretMenu(androidContext(), AscoltoSecretMenuConfiguration(androidContext()), get())
    }

    // Theirs - Lib
    single {
        Theirs(androidContext(), AscoltoTheirsConfiguration())
    }

    single {
        Sesame(AscoltoSesameConfiguration())
    }

    // Pico - Lib
    single {
        Pico(androidContext(), AscoltoPicoConfiguration(androidContext()))
    }

    // single instance of OracleRepository
    single<OracleRepository> { OracleRepositoryImpl(androidContext(), get(), get()) }

    // SetupViewModel
    viewModel { SetupViewModel(get()) }

    // HomeSharedViewModel
    viewModel { HomeSharedViewModel(get()) }

    // OnboardingViewModel
    viewModel { (handle: SavedStateHandle) -> OnboardingViewModel(handle, get()) }

    // LogViewModel
    viewModel { (handle: SavedStateHandle) -> LogViewModel(handle, get()) }
}
