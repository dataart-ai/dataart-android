package com.datartproject.dataart.client

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class HttpResponse(val statusCode: Int, val responseContent: String) {
    fun isSuccessful(): Boolean {
        return statusCode in 200..299
    }
}

class DataArtHttpClient(private val apiKey: String) {
    companion object {
        private const val prodBaseUrl = "https://src.datartproject.com"
        private const val baseUrl = prodBaseUrl

        private const val HTTP_METHOD_POST = "POST"
        private const val HTTP_HEADER_X_API_KEY = "X-API-Key"
        private const val HTTP_HEADER_USER_AGENT = "User-Agent"
        private const val HTTP_HEADER_CONTENT_TYPE = "Content-Type"
        private const val HTTP_HEADER_CONTENT_LENGTH = "Content-Length"

        private const val HTTP_STATUS_OK = 200
    }

    private enum class EndpointUrl {
        SendActions,
        Identity;

        fun build(): String {
            return when (this) {
                SendActions -> "$baseUrl/events/send-actions"
                Identity -> "$baseUrl/users/identify"
            }
        }
    }

    private fun post(endpointUrl: EndpointUrl, payload: String): HttpResponse {
        val url = URL(endpointUrl.build())
        with(url.openConnection() as HttpsURLConnection) {
            val bytes = payload.toByteArray()

            requestMethod = HTTP_METHOD_POST
            doOutput = true
            setFixedLengthStreamingMode(bytes.size)
            setRequestProperty(HTTP_HEADER_X_API_KEY, apiKey)
            setRequestProperty(HTTP_HEADER_USER_AGENT, "dataart-android")
            setRequestProperty(HTTP_HEADER_CONTENT_TYPE, "application/json; charset=UTF-8")
            setRequestProperty(HTTP_HEADER_CONTENT_LENGTH, "${bytes.size}")
            connect()
            outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }

            val response = StringBuffer()
            BufferedReader(InputStreamReader(inputStream)).use {
                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
            }

            return@post HttpResponse(responseCode, response.toString())
        }
    }

    fun postActions(payload: String): HttpResponse {
        return post(EndpointUrl.SendActions, payload)
    }

    fun postIdentity(payload: String): HttpResponse {
        return post(EndpointUrl.Identity, payload)
    }
}
