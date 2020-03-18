package com.bendingspoons.ascolto.ui.setup

import android.content.Context
import com.bendingspoons.ascolto.api.oracle.model.AscoltoSettings
import com.bendingspoons.ascolto.api.oracle.repository.OracleRepository
import com.bendingspoons.ascolto.db.AscoltoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

interface SetupRepository {
    suspend fun getOracleSetting(): Response<AscoltoSettings>
    suspend fun populateDb()
}

class SetupRepositoryImpl(
    val context: Context,
    val database: AscoltoDatabase,
    val oracleRepository: OracleRepository
) : SetupRepository {

    val dataFile = "form/form.json"
    val localesDataList = listOf(dataFile)

    override suspend fun getOracleSetting(): Response<AscoltoSettings> {
        return oracleRepository.settings()
    }

    override suspend fun populateDb() = withContext(Dispatchers.IO) {

    }
}