package org.immuni.android.networking.api

import android.content.Context
import org.immuni.android.base.http.GzipRequestInterceptor
import org.immuni.android.base.utils.DeviceInfoProviderImpl
import org.immuni.android.base.utils.DeviceUtils
import org.immuni.android.networking.NetworkingConfiguration
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.net.ssl.HostnameVerifier

class NetworkingRetrofit(
    context: Context,
    config: NetworkingConfiguration
) {
    private val idsManager = config.idsManager()

    // catch here all network or unexpected exceptions
    // in order to don't have to explicitly try/catch all the retrofit calls
    val exceptionsInterceptor: ((Interceptor.Chain) -> Response) = { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            Response.Builder()
                .request(request)
                .code(403)
                .body("{}".toResponseBody())
                .protocol(Protocol.HTTP_2)
                .message("Networking IO Exception")
                .headers(request.headers)
                .build()
        }
    }

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
                .addHeader("Pico-Unique-Id", idsManager.backupPersistentId.id)
                .addHeader("Pico-Client-Id", idsManager.nonBackupPersistentId.id)

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

        builder.addInterceptor(exceptionsInterceptor)
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