package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// MARK: - Fetch datafile content
internal fun FeaturevisorInstance.fetchDatafileContent(
    url: String,
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileContent>) -> Unit,
) {
    println("🔍 fetchDatafileContent called with URL: $url")

    handleDatafileFetch?.let { handleFetch ->
        println("📦 Using handleDatafileFetch override")
        val result = handleFetch(url)
        completion(result)
        return
    }

    println("🚀 Using direct HTTP fetch (Ktor version matching original OkHttp)")
    fetchDatafileContentFromUrl(url, completion, this.fetchCoroutineScope)
}

private fun fetchDatafileContentFromUrl(
    url: String,
    completion: (Result<DatafileContent>) -> Unit,
    coroutineScope: CoroutineScope,
) {
    println("🌐 Creating HTTP request for: $url")

    try {
        // Validate URL format (equivalent to toHttpUrl() validation)
        Url(url)
        println("✅ URL validation passed")

    } catch (throwable: Exception) {
        println("❌ URL validation failed: ${throwable.message}")
        completion(Result.failure(FeaturevisorError.InvalidUrl(url)))
        return
    }

    // Launch in the background, but use callback pattern like original
    coroutineScope.launch {
        val client = HttpClient()

        try {
            println("📤 Starting HTTP request...")

            val response = client.get(url) {
                headers {
                    // IMPORTANT: Match original exactly - it used Content-Type, not Accept
                    append(HttpHeaders.ContentType, "application/json")
                }
            }

            println("📥 HTTP response received: ${response.status}")

            // Handle response (equivalent to onResponse callback)
            handleResponse(response, completion)

        } catch (e: Exception) {
            // Handle failure (equivalent to onFailure callback)
            println("❌ HTTP request exception: ${e}: ${e.message}")
            e.printStackTrace()
            completion(Result.failure(e))
        } finally {
            client.close()
            println("🔌 HTTP client closed")
        }
    }
}

private suspend fun handleResponse(
    response: HttpResponse,
    completion: (Result<DatafileContent>) -> Unit
) {
    try {
        val responseBodyString = response.bodyAsText()
        println("📄 Response body length: ${responseBodyString.length}")

        if (response.status.isSuccess()) {
            println("✅ HTTP request successful, parsing response...")

            // Create Json instance matching original configuration
            val json = Json {
                ignoreUnknownKeys = true
            }

            // Log response like original
            FeaturevisorInstance.companionLogger?.debug(responseBodyString)

            try {
                val content = json.decodeFromString<DatafileContent>(responseBodyString)
                println("✅ Successfully parsed DatafileContent: ${content.features.size} features")
                completion(Result.success(content))

            } catch (throwable: Throwable) {
                println("❌ JSON parsing failed: ${throwable.message}")
                throwable.printStackTrace()

                completion(
                    Result.failure(
                        FeaturevisorError.UnparsableJson(
                            responseBodyString,
                            response.status.description
                        )
                    )
                )
            }
        } else {
            println("❌ HTTP request failed: ${response.status}")

            // Match original error handling - both cases used UnparsableJson
            completion(
                Result.failure(
                    FeaturevisorError.UnparsableJson(
                        responseBodyString,
                        response.status.description
                    )
                )
            )
        }
    } catch (e: Exception) {
        println("❌ Response handling error: ${e}: ${e.message}")
        e.printStackTrace()
        completion(Result.failure(e))
    }
}