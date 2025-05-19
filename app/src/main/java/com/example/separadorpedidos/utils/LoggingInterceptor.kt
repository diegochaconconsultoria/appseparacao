package com.example.separadorpedidos.utils

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

/**
 * Interceptor para logar requisições e respostas HTTP
 */
class LoggingInterceptor : Interceptor {

    private val TAG = "API_DEBUG"
    private val UTF8 = Charset.forName("UTF-8")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val requestBody = request.body
        var body = ""
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            body = buffer.readString(UTF8)
        }

        Log.d(TAG, "Request: ${request.method} ${request.url}")
        Log.d(TAG, "Headers: ${request.headers}")
        Log.d(TAG, "Body: $body")

        val startTime = System.nanoTime()
        val response = chain.proceed(request)
        val tookTime = System.nanoTime() - startTime

        val responseBody = response.body
        val bodyString = if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            val contentType = responseBody.contentType()
            val charset = contentType?.charset(UTF8) ?: UTF8

            // Se for um tipo de imagem ou arquivo binário, apenas log o tipo e tamanho
            if (contentType?.toString()?.contains("image") == true) {
                "[Conteúdo binário: ${contentType}, tamanho: ${buffer.size} bytes]"
            } else {
                buffer.clone().readString(charset)
            }
        } else {
            "null"
        }

        Log.d(TAG, "Response: ${response.code} (${tookTime / 1e6}ms)")
        Log.d(TAG, "Headers: ${response.headers}")
        Log.d(TAG, "Body: $bodyString")

        // Recria o body para não consumir o original
        val contentType = responseBody?.contentType()
        val contentLength = bodyString.length.toLong()
        val newBody = ResponseBody.create(contentType, bodyString)

        return response.newBuilder()
            .body(newBody)
            .build()
    }
}