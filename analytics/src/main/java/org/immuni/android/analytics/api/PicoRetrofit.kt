package org.immuni.android.analytics.api

import org.immuni.android.extensions.http.GzipRequestInterceptor
import org.immuni.android.ids.IdsManager
import org.immuni.android.analytics.Pico
import org.immuni.android.analytics.PicoConfiguration
import org.immuni.android.analytics.util.BS_NUMBER_JSON_SERIALIZATION_ADAPTER
import org.immuni.android.analytics.model.PicoUserAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import javax.net.ssl.HostnameVerifier

class PicoRetrofit(config: PicoConfiguration) {
    private val ids: IdsManager = config.idsManager()

    // catch here all network or unexpected exceptions
    // in order not to have to explicitly try/catch all retrofit calls
    private val exceptionsInterceptor: ((Interceptor.Chain) -> Response) = { chain ->
        val request = chain.request()
        try {
            chain.proceed(request)
        } catch (e: Exception) {
            Response.Builder()
                .request(request)
                .code(403)
                .body("{}".toResponseBody())
                .protocol(Protocol.HTTP_2)
                .message("Pico lib IO Exception")
                .headers(request.headers)
                .build()
        }
    }

    private val headersInterceptor: ((Interceptor.Chain) -> Response) = { chain ->
        chain.run {
            proceed(
                request()
                    .newBuilder()
                    .addHeader("Pico-Version", Pico.VERSION)
                    .addHeader("Pico-Client-ID", ids.id.id)
                    .addHeader("Pico-Tester", config.isDevelopmentDevice().toString())
                    .build()
            )
        }
    }

    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    private val certificatePinner = config.certificatePinner()
    private val gzipInterceptor = GzipRequestInterceptor()

    val client by lazy {
        val builder = OkHttpClient.Builder()
            .hostnameVerifier(HostnameVerifier { hostname, session -> true })
            .addInterceptor(exceptionsInterceptor)
            //.addInterceptor(gzipInterceptor)
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)

        certificatePinner?.let {
            builder.certificatePinner(it)
        }

        builder.build()
    }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(config.endpoint())
        .client(client)
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(BS_NUMBER_JSON_SERIALIZATION_ADAPTER)
                    .add(PicoUserAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                    .build()
            ).asLenient()
        )
        .build()
}
