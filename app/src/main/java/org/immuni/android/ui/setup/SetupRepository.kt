package org.immuni.android.ui.setup

import android.content.Context
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.api.oracle.repository.OracleRepository
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.api.oracle.model.ImmuniMe
import retrofit2.Response

interface SetupRepository {
    suspend fun getOracleSetting(): Response<ImmuniSettings>
    suspend fun getOracleMe(): Response<ImmuniMe>
}

class SetupRepositoryImpl(
    val context: Context,
    val database: ImmuniDatabase,
    val oracleRepository: OracleRepository
) : SetupRepository {

    override suspend fun getOracleSetting(): Response<ImmuniSettings> {
        return oracleRepository.settings()
    }

    override suspend fun getOracleMe(): Response<ImmuniMe> {
        return oracleRepository.me()
    }
}