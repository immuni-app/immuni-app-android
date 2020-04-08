package com.bendingspoons.sesame

import com.bendingspoons.base.extensions.toHex
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Sesame(private val config: SesameConfiguration) {
    fun sign(
        request: Request,
        timestamp: Long,
        requestId: UUID
    ): String {
        val messageString = String.format(
            "%s:%s:%s:%s",
            request.url.toString().replace("https://", "http://"),
            request.bodyString(),
            timestamp.toString(),
            requestId.toString()
        )
        val messageByteArray = messageString.toByteArray(charset("UTF-8"))
        val mac = Mac.getInstance("HmacSha512").apply {
            init(SecretKeySpec(config.secretKey.toByteArray(), "HmacSha512"))
        }
        return mac.doFinal(messageByteArray).toHex().toLowerCase(Locale.ROOT)
    }

    val interceptor: ((Interceptor.Chain) -> Response) = { chain ->
        chain.run {
            val timestamp = System.currentTimeMillis()
            val requestId = UUID.randomUUID()

            val signature = sign(request(), timestamp, requestId)

            proceed(
                request()
                    .newBuilder()
                    .addHeader("Sesame-Timestamp", timestamp.toString())
                    .addHeader("Sesame-Request-Id", requestId.toString())
                    .addHeader("Sesame-Signature", signature)
                    .addHeader("Sesame-Protocol", "Sha512")
                    .build()
            )
        }
    }
}

fun Request.bodyString(): String {
    return try {
        val copy = this.newBuilder().build()
        val buffer = Buffer()
        copy.body?.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: IOException) {
        ""
    }
}
