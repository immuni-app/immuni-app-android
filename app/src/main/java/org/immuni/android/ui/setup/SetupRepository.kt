package org.immuni.android.ui.setup

import android.content.Context
import org.immuni.android.api.ImmuniAPIRepository
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.api.model.ImmuniMe
import retrofit2.Response

interface SetupRepository {
    suspend fun getOracleSetting(): Response<ImmuniSettings>
    suspend fun getOracleMe(): Response<ImmuniMe>
}

class SetupRepositoryImpl(
    val context: Context,
    val database: ImmuniDatabase,
    val immuniAPIRepository: ImmuniAPIRepository
) : SetupRepository {

    override suspend fun getOracleSetting(): Response<ImmuniSettings> {
        return immuniAPIRepository.settings()
    }

    override suspend fun getOracleMe(): Response<ImmuniMe> {
        return immuniAPIRepository.me()
    }
}