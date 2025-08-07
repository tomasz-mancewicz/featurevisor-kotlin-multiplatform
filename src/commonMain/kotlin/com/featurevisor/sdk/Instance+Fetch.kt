package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*

// MARK: - Fetch datafile content
internal suspend fun FeaturevisorInstance.fetchDatafileContent(
    url: String,
    handleDatafileFetch: DatafileFetchHandler? = null,
    completion: (Result<DatafileContent>) -> Unit,
) {
    handleDatafileFetch?.let { handleFetch ->
        val result = handleFetch(url)
        completion(result)
    } ?: run {
        fetchDatafileContentFromUrl(url, completion)
    }
}

private suspend fun fetchDatafileContentFromUrl(
    url: String,
    completion: (Result<DatafileContent>) -> Unit,
) {
    try {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val response = client.get(url) {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        client.close()

        if (response.status.isSuccess()) {
            val responseBodyString = response.bodyAsText()
            FeaturevisorInstance.companionLogger?.debug(responseBodyString)

            try {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val content = json.decodeFromString<DatafileContent>(responseBodyString)
                completion(Result.success(content))
            } catch (throwable: Throwable) {
                completion(
                    Result.failure(
                        FeaturevisorError.UnparsableJson(
                            responseBodyString,
                            throwable.message ?: "Failed to parse JSON"
                        )
                    )
                )
            }
        } else {
            completion(
                Result.failure(
                    FeaturevisorError.FetchingDataFileFailed(
                        "HTTP ${response.status.value}: ${response.status.description}"
                    )
                )
            )
        }
    } catch (throwable: Throwable) {
        completion(Result.failure(throwable))
    }
}