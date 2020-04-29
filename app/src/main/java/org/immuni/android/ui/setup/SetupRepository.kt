package org.immuni.android.ui.setup

import android.content.Context
import org.immuni.android.api.ImmuniAPIRepository
import org.immuni.android.api.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import org.immuni.android.api.model.ImmuniMe
import org.immuni.android.networking.api.ErrorResponse
import org.immuni.android.networking.api.NetworkResource
import retrofit2.Response

interface SetupRepository {
    suspend fun getOracleSetting(): NetworkResource<ImmuniSettings, ErrorResponse>
    suspend fun getOracleMe(): NetworkResource<ImmuniMe, ErrorResponse>
}

class SetupRepositoryImpl(
    val context: Context,
    val database: ImmuniDatabase,
    val immuniAPIRepository: ImmuniAPIRepository
) : SetupRepository {

    override suspend fun getOracleSetting(): NetworkResource<ImmuniSettings, ErrorResponse> {
        return immuniAPIRepository.settings()
    }

    override suspend fun getOracleMe(): NetworkResource<ImmuniMe, ErrorResponse> {
        return immuniAPIRepository.me()
    }
}