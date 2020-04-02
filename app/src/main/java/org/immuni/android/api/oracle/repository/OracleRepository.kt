package org.immuni.android.api.oracle.repository

import android.content.Context
import org.immuni.android.api.oracle.CustomOracleAPI
import org.immuni.android.api.oracle.model.ImmuniMe
import org.immuni.android.api.oracle.model.ImmuniSettings
import org.immuni.android.db.ImmuniDatabase
import com.bendingspoons.oracle.Oracle
import retrofit2.Response

interface OracleRepository {
    suspend fun settings(): Response<ImmuniSettings>
    suspend fun me(): Response<ImmuniMe>
}

class OracleRepositoryImpl(
    val context: Context,
    val database: ImmuniDatabase,
    val oracle: Oracle<ImmuniSettings, ImmuniMe>
) : OracleRepository {

    private val customApi = oracle.customServiceAPI(CustomOracleAPI::class)

    override suspend fun settings(): Response<ImmuniSettings> {
        return oracle.api.fetchSettings()
    }

    override suspend fun me(): Response<ImmuniMe> {
        return oracle.api.fetchMe()
    }
}
