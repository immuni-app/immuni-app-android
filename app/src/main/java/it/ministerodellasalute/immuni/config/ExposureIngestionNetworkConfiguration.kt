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

package it.ministerodellasalute.immuni.config

import android.content.Context
import com.squareup.moshi.Moshi
import it.ministerodellasalute.immuni.R
import it.ministerodellasalute.immuni.logic.settings.ConfigurationSettingsManager
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer

class ExposureIngestionNetworkConfiguration(
    context: Context,
    private val settingsManager: ConfigurationSettingsManager,
    override val moshi: Moshi
) : BaseNetworkConfiguration(context, moshi) {
    class Interceptor(private val tekPacketSize: () -> Int) : okhttp3.Interceptor {
        override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
            val request = chain.request()
            val body = request.body!!
            val requestSize = request.url.encodedPath.length +
                request.headers.byteCount() +
                request.method.length +
                body.contentLength()

            val buffer = Buffer()
            body.writeTo(buffer)
            val bodyString = buffer.readUtf8()

            val paddingSize = tekPacketSize() - requestSize.toInt()
            val padding = "0".repeat(paddingSize)
            val paddedRequest = request.newBuilder().post(
                bodyString
                    .replace("padding\":\"\"", "padding\":\"$padding\"")
                    .toRequestBody(body.contentType())
            ).build()

            return chain.proceed(paddedRequest)
        }
    }

    override fun baseUrl(): String {
        return context.getString(R.string.upload_base_url)
    }

    override fun interceptors() = listOf(
        Interceptor { settingsManager.settings.value.teksPacketSize }
    )
}
