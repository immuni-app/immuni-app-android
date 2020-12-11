/*
 * Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 * Please refer to the AUTHORS file for more information.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.ministerodellasalute.immuni.nearby

import android.app.Activity
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.android.gms.common.api.ApiException
import it.ministerodellasalute.immuni.extensions.file.copyInputStream
import it.ministerodellasalute.immuni.extensions.lifecycle.AppLifecycleObserver
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationClient
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager
import it.ministerodellasalute.immuni.extensions.nearby.ExposureNotificationManager.Delegate
import java.io.File
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyMatchingTest {
    private val testContext get() = getInstrumentation().context
    private val context get() = getInstrumentation().targetContext

    @Test(expected = ApiException::class)
    fun testProvideDiagnosisKeysWithCorruptFile() {
        return provideDiagnosisKeys("corrupt")
    }

    @Test(expected = ApiException::class)
    fun testProvideDiagnosisKeysWithWrongFileName() {
        return provideDiagnosisKeys("wrong_filename")
    }

    @Test
    fun testProvideDiagnosisKeysWithEmptyFile() {
        return provideDiagnosisKeys("empty")
    }

    @Test
    fun testProvideDiagnosisKeysWithLegitFile() {
        return provideDiagnosisKeys("legit")
    }

    private fun provideDiagnosisKeys(name: String) = runBlocking {
        val dstFolder = File(context.dataDir.path + File.separator + "key_samples")
        dstFolder.deleteRecursively()
        dstFolder.mkdir()
        val fileName = "key_samples${File.separator}$name.zip"
        val inputStream = testContext.resources.assets.open(fileName)
        val dstFile = File(dstFolder.path + File.separator + name + ".zip")
        dstFile.copyInputStream(inputStream)

        val manager = ExposureNotificationManager(context, AppLifecycleObserver()).apply {
            setup(object : Delegate {
                override suspend fun processKeys(
                    serverDate: Date,
                    summary: ExposureNotificationClient.ExposureSummary,
                    getInfos: suspend () -> List<ExposureNotificationClient.ExposureInformation>
                ) {}
            })
        }

        val token = UUID.randomUUID().toString()

        // it is needed an activity to run the Exposure API
        val scenario = launchActivity<NearbyTestActivity>()
        lateinit var activity: Activity
        scenario.onActivity { activity = it }
        manager.optInAndStartExposureTracing(activity)
        manager.provideDiagnosisKeys(
            keyFiles = listOf(dstFile),
            configuration = ExposureNotificationClient.ExposureConfiguration(
                minimumRiskScore = 1,
                attenuationScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                daysSinceLastExposureScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                durationScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                transmissionRiskScores = listOf(1, 2, 3, 4, 5, 6, 7, 8),
                attenuationThresholds = listOf(50, 70)
            ),
            token = token
        )
    }
}
