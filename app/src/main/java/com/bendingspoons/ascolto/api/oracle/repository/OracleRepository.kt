package com.bendingspoons.ascolto.api.oracle.repository

import android.content.Context
import com.bendingspoons.ascolto.api.oracle.CustomOracleAPI
import com.bendingspoons.ascolto.api.oracle.model.AscoltoMe
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.ascolto.db.AscoltoDatabase
import com.bendingspoons.oracle.Oracle
import retrofit2.Response

interface OracleRepository {
    suspend fun settings(): Response<AscoltoSettings>
    suspend fun me(): Response<AscoltoMe>
}

class OracleRepositoryImpl(
    val context: Context,
    val database: AscoltoDatabase,
    val oracle: Oracle<AscoltoSettings, AscoltoMe>
) : OracleRepository {

    private val customApi = oracle.customServiceAPI(CustomOracleAPI::class)

    override suspend fun settings(): Response<AscoltoSettings> {
        return oracle.api.fetchSettings()
    }

    override suspend fun me(): Response<AscoltoMe> {
        return oracle.api.fetchMe()
    }
}
