package org.immuni.android.network.api

import org.immuni.android.extensions.http.GzipRequestInterceptor
import org.immuni.android.network.NetworkConfiguration
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

/**
 * Initialize Retrofit type-safe HTTP API client.
 *
 * It uses some [Interceptor] for logging and gZip requests.
 * It uses certificate pinning for security reason.
 * It uses [Moshi] to serialize and deserialize JSON.
 */
class NetworkRetrofit(
    config: NetworkConfiguration
) {

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }
    var certificatePinner = config.certificatePinner()
    val gzipInterceptor = GzipRequestInterceptor()

    val client by lazy {
        val builder = OkHttpClient.Builder()
        //.hostnameVerifier(HostnameVerifier { hostname, session -> true })

        builder.addInterceptor(gzipInterceptor)
        builder.addInterceptor(loggingInterceptor)
        // use this to verify cache
        // network interceptor is not called when using cache
        //builder.addNetworkInterceptor(loggingInterceptor)

        certificatePinner?.let {
            builder.certificatePinner(it)
        }

        builder.build()
    }

    val retrofit = Retrofit.Builder()
        .baseUrl(config.endpoint())
        .client(client)
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                    .build()
            ).asLenient()
        )
        .build()
}