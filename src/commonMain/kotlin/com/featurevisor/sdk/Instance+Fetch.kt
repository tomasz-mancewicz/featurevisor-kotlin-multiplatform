package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// MARK: - Fetch datafile content
internal fun FeaturevisorInstance.fetchDatafileContent(
    url: String,
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileContent>) -> Unit,
) {
    fun debugLog(message: String, data: Map<String, Any>? = null) {
        logger?.debug(message, data ?: emptyMap())
        println("Featurevisor.fetch: $message") // Fallback
    }

    fun errorLog(message: String, error: Throwable? = null) {
        logger?.error(message, if (error != null) mapOf("error" to error) else emptyMap())
        println("Featurevisor.fetch ERROR: $message ${error?.message ?: ""}")
    }

    debugLog("🔍 fetchDatafileContent called", mapOf(
        "url" to url,
        "hasHandler" to (handleDatafileFetch != null)
    ))

    try {
        handleDatafileFetch?.let { handleFetch ->
            debugLog("📦 Using handleDatafileFetch override")
            try {
                val result = handleFetch(url)
                debugLog("📦 handleDatafileFetch returned", mapOf("success" to result.isSuccess))
                if (result.isFailure) {
                    errorLog("📦 handleDatafileFetch error", result.exceptionOrNull())
                }
                completion(result)
            } catch (e: Exception) {
                errorLog("📦 handleDatafileFetch threw exception", e)
                completion(Result.failure(e))
            }
            return
        }

        debugLog("🚀 Using direct HTTP fetch (clean Ktor version)")
        fetchDatafileContentFromUrl(url, completion, this.fetchCoroutineScope)

    } catch (e: Exception) {
        errorLog("❌ Exception in fetchDatafileContent", e)
        completion(Result.failure(e))
    }
}

private fun fetchDatafileContentFromUrl(
    url: String,
    completion: (Result<DatafileContent>) -> Unit,
    coroutineScope: CoroutineScope,
) {
    println("🌐 Creating HTTP request for: $url")
    println("🌐 Using EXACT same client setup as working manual test")

    coroutineScope.launch {
        try {
            println("📤 Starting HTTP request...")

            // Use the EXACT same HttpClient setup as your working manual test
            val client = HttpClient {
                // Copy your working client configuration here
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
            }

            val response = client.get(url)

            println("✅ HTTP Status: ${response.status}")
            println("✅ Content-Type: ${response.headers["Content-Type"]}")

            val content = response.bodyAsText()
            println("✅ Response length: ${content.length}")

            client.close()

            if (response.status.isSuccess()) {
                println("✅ HTTP request successful, parsing response...")

                try {
                    // Use the same parsing approach as your manual test
                    val datafileContent = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }.decodeFromString<DatafileContent>(content)

                    println("✅ Successfully parsed DatafileContent")
                    println("   - Schema: ${datafileContent.schemaVersion}")
                    println("   - Revision: ${datafileContent.revision}")
                    println("   - Features count: ${datafileContent.features.size}")

                    completion(Result.success(datafileContent))

                } catch (e: Exception) {
                    println("❌ JSON parsing failed: ${e.message}")
                    e.printStackTrace()
                    completion(Result.failure(e))
                }
            } else {
                println("❌ HTTP request failed: ${response.status}")
                completion(Result.failure(Exception("HTTP error: ${response.status}")))
            }

        } catch (e: Exception) {
            println("❌ Network error: ${e.message}")
            e.printStackTrace()
            completion(Result.failure(e))
        }
    }
}