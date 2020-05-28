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

package it.ministerodellasalute.immuni.network.api

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import it.ministerodellasalute.immuni.extensions.http.TrafficAnalysisPreventionHeadersInterceptor
import it.ministerodellasalute.immuni.network.BuildConfig
import it.ministerodellasalute.immuni.network.NetworkConfiguration
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Initialize Retrofit type-safe HTTP API client.
 *
 * It uses some [Interceptor] for logging and gZip requests.
 * It uses certificate pinning for security reason.
 * It uses [Moshi] to serialize and deserialize JSON.
 */
class NetworkRetrofit(
    context: Context,
    config: NetworkConfiguration
) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }
    private val certificatePinner = config.certificatePinner()
    private val headersInterceptor = TrafficAnalysisPreventionHeadersInterceptor()

    private val client by lazy {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(headersInterceptor)

        config.interceptors().forEach { builder.addInterceptor(it) }

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor)
        }

        /**
         * uncomment this line to verify if http cache is working
         * network interceptor is not called when the cache is used.
         **/
        // builder.addNetworkInterceptor(loggingInterceptor)

        certificatePinner?.let {
            builder.certificatePinner(it)
        }

        if (config.useCacheHeaders()) {
            val cacheSize = 2 * 1024 * 1024
            try {
                val cache = Cache(context.cacheDir, cacheSize.toLong())
                builder.cache(cache)
            } catch (e: Exception) {
                Log.d("NetworkRetrofit", "Unable to set OkHttp cache.")
            }
        }

        builder.build()
    }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl())
        .client(client)
        .addConverterFactory(
            MoshiConverterFactory.create(
                config.moshi
            )
        )
        .build()
}
