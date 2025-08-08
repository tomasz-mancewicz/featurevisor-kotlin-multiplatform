package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
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

    try {
        // Validate URL format
        Url(url)
        println("✅ URL validation passed")

    } catch (throwable: Exception) {
        println("❌ URL validation failed: ${throwable.message}")
        completion(Result.failure(FeaturevisorError.InvalidUrl(url)))
        return
    }

    // Launch coroutine for HTTP request
    coroutineScope.launch {
        println("🧵 Coroutine started for HTTP request")

        // Create simple HTTP client WITHOUT any plugins
        val client = HttpClient()
        println("🏭 Basic HTTP client created")

        try {
            println("📤 Making GET request...")

            val response = client.get(url) {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                    println("📋 Added Content-Type header")
                }
            }

            println("📥 Response received: ${response.status}")

            // Get response body as text
            val responseBodyString = response.bodyAsText()
            println("📄 Response body received: ${responseBodyString.length} characters")

            if (response.status.isSuccess()) {
                println("✅ HTTP request successful, parsing JSON...")

                // Log response for debugging
                FeaturevisorInstance.companionLogger?.debug(responseBodyString)

                try {
                    // Parse JSON manually using kotlinx.serialization
                    val json = Json {
                        ignoreUnknownKeys = true
                    }
                    println("🔧 JSON parser configured")

                    val content = json.decodeFromString<DatafileContent>(responseBodyString)
                    println("✅ Successfully parsed DatafileContent: ${content.features.size} features, revision ${content.revision}")

                    completion(Result.success(content))

                } catch (throwable: Throwable) {
                    println("❌ JSON parsing failed: ${throwable}: ${throwable.message}")
                    throwable.printStackTrace()

                    completion(
                        Result.failure(
                            FeaturevisorError.UnparsableJson(
                                responseBodyString,
                                throwable.message ?: "JSON parsing failed"
                            )
                        )
                    )
                }
            } else {
                println("❌ HTTP request failed: ${response.status}")

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
            println("❌ HTTP request exception: ${e}: ${e.message}")
            e.printStackTrace()
            completion(Result.failure(e))

        } finally {
            client.close()
            println("🔌 HTTP client closed")
        }
    }
}