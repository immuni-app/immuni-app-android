package org.ascolto.onlus.geocrowd19.android.ui.setup

import android.content.Context
import org.ascolto.onlus.geocrowd19.android.api.oracle.model.AscoltoSettings
import org.ascolto.onlus.geocrowd19.android.api.oracle.repository.OracleRepository
import org.ascolto.onlus.geocrowd19.android.db.AscoltoDatabase
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