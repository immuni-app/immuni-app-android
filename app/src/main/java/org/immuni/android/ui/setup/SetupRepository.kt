package org.immuni.android.ui.setup

import android.content.Context
import org.immuni.android.api.oracle.model.AscoltoSettings
import org.immuni.android.api.oracle.repository.OracleRepository
import org.immuni.android.db.AscoltoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.immuni.android.api.oracle.model.AscoltoMe
import retrofit2.Response

interface SetupRepository {
    suspend fun getOracleSetting(): Response<AscoltoSettings>
    suspend fun getOracleMe(): Response<AscoltoMe>
}

class SetupRepositoryImpl(
    val context: Context,
    val database: AscoltoDatabase,
    val oracleRepository: OracleRepository
) : SetupRepository {

    override suspend fun getOracleSetting(): Response<AscoltoSettings> {
        return oracleRepository.settings()
    }

    override suspend fun getOracleMe(): Response<AscoltoMe> {
        return oracleRepository.me()
    }
}