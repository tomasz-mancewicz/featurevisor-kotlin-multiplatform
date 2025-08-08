package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.coroutines.launch

private val sharedJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

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

    println("🚀 Launching coroutine to fetch datafile from URL")

    this.fetchCoroutineScope.launch {
        val result = fetchDatafileContentFromUrl(url)
        println("✅ Coroutine completed with result: ${result.isSuccess}")
        completion(result)
    }
}


private suspend fun fetchDatafileContentFromUrl(
    url: String,
): Result<DatafileContent> {
    println("🌐 Attempting to fetch datafile from: $url")

    return try {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(sharedJson)
            }
        }

        val response = client.get(url) {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        println("📥 HTTP response received: ${response.status}")

        client.close()

        val responseBodyString = response.bodyAsText()

        FeaturevisorInstance.companionLogger?.debug(responseBodyString)
        println("📄 Response body (truncated): ${responseBodyString.take(100)}")

        if (response.status.isSuccess()) {
            try {
                val content = sharedJson.decodeFromString<DatafileContent>(responseBodyString)
                println("✅ Successfully parsed datafile content")
                Result.success(content)
            } catch (e: Throwable) {
                println("❌ Failed to parse JSON: ${e.message}")
                Result.failure(
                    FeaturevisorError.UnparsableJson(
                        responseBodyString,
                        e.message ?: "Failed to parse JSON"
                    )
                )
            }
        } else {
            println("❌ HTTP error: ${response.status.value} ${response.status.description}")
            Result.failure(
                FeaturevisorError.FetchingDataFileFailed(
                    "HTTP ${response.status.value}: ${response.status.description}"
                )
            )
        }
    } catch (e: Throwable) {
        println("❌ Exception while fetching datafile: ${e.message}")
        Result.failure(e)
    }
}
