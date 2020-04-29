package org.immuni.android.ui.setup

import android.content.Context
import org.immuni.android.networking.ApiManager
import org.immuni.android.networking.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.networking.model.ImmuniMe
import retrofit2.Response

interface SetupRepository {
    suspend fun getOracleSetting(): Response<ImmuniSettings>
    suspend fun getOracleMe(): Response<ImmuniMe>
}

class SetupRepositoryImpl(
    val context: Context,
    val database: ImmuniDatabase,
    val apiManager: ApiManager
) : SetupRepository {

    override suspend fun getOracleSetting(): Response<ImmuniSettings> {
        return apiManager.settings()
    }

    override suspend fun getOracleMe(): Response<ImmuniMe> {
        return apiManager.me()
    }
}