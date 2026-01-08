package com.example.terminal_test_app.platform.endPointTest

import com.example.terminal_test_app.domain.model.EndpointTestResult
import com.example.terminal_test_app.domain.repository.EndpointTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import okhttp3.Request

class AndroidEndpointTester : EndpointTester {

    private val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    override suspend fun test(url: String): EndpointTestResult =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .head() // or .get()
                    .build()

                val start = System.currentTimeMillis()
                val response = client.newCall(request).execute()
                val latency = System.currentTimeMillis() - start

                EndpointTestResult.Success(
                    statusCode = response.code,
                    latencyMs = latency
                )
            } catch (e: Exception) {
                EndpointTestResult.Failure(
                    reason = e.localizedMessage ?: "Unknown error"
                )
            }
        }
}
