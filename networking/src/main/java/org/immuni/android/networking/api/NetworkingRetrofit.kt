package org.immuni.android.networking.api

import android.content.Context
import org.immuni.android.extensions.http.GzipRequestInterceptor
import org.immuni.android.extensions.utils.DeviceInfoProviderImpl
import org.immuni.android.extensions.utils.DeviceUtils
import org.immuni.android.networking.NetworkingConfiguration
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.net.ssl.HostnameVerifier

class NetworkingRetrofit(
    context: Context,
    config: NetworkingConfiguration
) {
    private val idsManager = config.ids().manager

    val headersInterceptor: ((Interceptor.Chain) -> Response) = { chain ->
        val infoProvider = DeviceInfoProviderImpl()
        chain.run {
            val requestBuilder = request()
                .newBuilder()
                .addHeader("Locale", Locale.getDefault().country)
                .addHeader("Language", Locale.getDefault().language)
                .addHeader("Device-Type", "android")
                .addHeader("iOS-Platform", infoProvider.devicePlatform())
                .addHeader("iOS-Version", infoProvider.androidVersion())
                .addHeader("Android-Platform", infoProvider.devicePlatform())
                .addHeader("Android-Version", infoProvider.androidVersion())
                .addHeader("Build", DeviceUtils.appVersionCode(context).toString())
                .addHeader("Pico-Unique-Id", idsManager.id.id)
                .addHeader("Pico-Client-Id", idsManager.id.id)

            proceed(requestBuilder.build())
        }
    }

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    var certificatePinner = config.certificatePinner()

    val gzipInterceptor = GzipRequestInterceptor()

    val client by lazy {
        val builder = OkHttpClient.Builder()
            .hostnameVerifier(HostnameVerifier { hostname, session -> true })

        builder.addInterceptor(headersInterceptor)
        builder.addInterceptor(gzipInterceptor)

        builder.addInterceptor(loggingInterceptor)

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