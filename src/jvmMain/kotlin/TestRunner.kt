import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.InstanceOptions
import com.featurevisor.sdk.Logger
import com.featurevisor.types.DatafileContent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json

fun main() {
    println("🚀 Featurevisor JVM Test Runner")
    println("==============================")

    runBlocking {
        try {
            // Test 1: Basic HTTP test
            testBasicHttp()

            println("\n" + "=".repeat(50) + "\n")

            // Test 2: Featurevisor test
            testFeaturevisor()

        } catch (e: Exception) {
            println("💣 Test runner failed: ${e.message}")
            e.printStackTrace()
        }
    }

    println("\n🏁 Test runner completed")
}

suspend fun testBasicHttp() {
    println("🔍 TEST 1: Basic HTTP Request")
    println("-".repeat(30))

    try {
        val client = HttpClient()
        println("✅ HttpClient created")

        val response = client.get("https://features.fe.indazn.com/staging/datafile-tag-android.json")
        println("✅ Response status: ${response.status}")
        println("✅ Content-Type: ${response.headers["Content-Type"]}")

        val content = response.bodyAsText()
        println("✅ Content length: ${content.length}")
        println("✅ First 200 chars: ${content.take(200)}")

        // Test JSON parsing
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val datafileContent = json.decodeFromString<DatafileContent>(content)
        println("✅ JSON parsed successfully:")
        println("   - Schema: ${datafileContent.schemaVersion}")
        println("   - Revision: ${datafileContent.revision}")
        println("   - Features: ${datafileContent.features.size}")

        client.close()
        println("🎉 TEST 1 PASSED: Basic HTTP + JSON parsing works!")

    } catch (e: Exception) {
        println("❌ TEST 1 FAILED: ${e.javaClass.simpleName}")
        println("   Error: ${e.message}")
        e.printStackTrace()
    }
}

suspend fun testFeaturevisor() {
    println("🔍 TEST 2: Featurevisor Initialization")
    println("-".repeat(30))

    try {
        // Create logger
        val logger = Logger.createLogger(
            levels = listOf(
                Logger.LogLevel.DEBUG,
                Logger.LogLevel.INFO,
                Logger.LogLevel.WARN,
                Logger.LogLevel.ERROR
            ),
            handle = { level, message, details ->
                println("📋 FVISOR-${level.value.uppercase()}: $message")
                if (!details.isNullOrEmpty()) {
                    details.forEach { (key, value) ->
                        println("   $key: $value")
                    }
                }
            }
        )
        println("✅ Logger created")

        var testResult: String? = null
        val testComplete = CompletableDeferred<Unit>()

        val options = InstanceOptions(
            datafileUrl = "https://features.fe.indazn.com/staging/datafile-tag-android.json",
            logger = logger,
            onReady = { args ->
                println("🎉 FEATUREVISOR READY CALLBACK!")
                println("   Args received: ${args.size} arguments")

                // The datafile should be the first (and likely only) argument
                val datafile = args.getOrNull(0)

                when (datafile) {
                    is DatafileContent -> {
                        println("   ✅ Datafile type: DatafileContent")
                        println("   ✅ Schema version: ${datafile.schemaVersion}")
                        println("   ✅ Revision: ${datafile.revision}")
                        println("   ✅ Features count: ${datafile.features.size}")
                        println("   ✅ Attributes count: ${datafile.attributes.size}")
                        println("   ✅ Segments count: ${datafile.segments.size}")

                        // Show first few feature names
                        if (datafile.features.isNotEmpty()) {
                            println("   ✅ Sample features:")
                            datafile.features.take(3).forEach { feature ->
                                println("      - ${feature.key}")
                            }
                        }

                        testResult = "SUCCESS"
                    }
                    else -> {
                        println("   ❌ Unexpected datafile type: ${datafile?.javaClass?.simpleName}")
                        println("   📊 All arguments:")
                        args.forEachIndexed { index, arg ->
                            println("      [$index] ${arg?.javaClass?.simpleName}: $arg")
                        }
                        testResult = "WRONG_TYPE: ${datafile?.javaClass?.simpleName}"
                    }
                }
                testComplete.complete(Unit)
            },
            onError = { args ->
                println("💥 FEATUREVISOR ERROR CALLBACK!")
                println("   Args received: ${args.size} arguments")

                val error = args.getOrNull(0)

                when {
                    error is Throwable -> {
                        println("   🐛 Error is Throwable: ${error.javaClass.simpleName}")
                        println("   💬 Message: ${error.message}")
                        println("   📚 Stack trace:")
                        error.printStackTrace()
                        testResult = "EXCEPTION: ${error.message}"
                    }
                    args.isNotEmpty() -> {
                        println("   📊 All error arguments:")
                        args.forEachIndexed { index, arg ->
                            println("      [$index] ${arg?.javaClass?.simpleName}: $arg")
                            if (arg is Throwable) {
                                println("         Exception: ${arg.message}")
                                arg.printStackTrace()
                            }
                        }
                        testResult = "ERROR: ${args.contentToString()}"
                    }
                    else -> {
                        println("   ❓ Empty error array")
                        testResult = "EMPTY_ERROR"
                    }
                }
                testComplete.complete(Unit)
            }
        )

        println("📦 Creating Featurevisor instance...")
        val featurevisor = FeaturevisorInstance.createInstance(options)
        println("✅ Featurevisor instance created")
        println("⏳ Waiting for initialization...")

        // Wait for completion with timeout
        withTimeoutOrNull(30000) {
            testComplete.await()
        } ?: run {
            println("⏰ TIMEOUT: No response after 30 seconds")
            testResult = "TIMEOUT"
        }

        // Final result
        println("\n🏆 FINAL RESULT:")
        when {
            testResult?.startsWith("SUCCESS") == true -> {
                println("✅ TEST 2 PASSED: Featurevisor works perfectly!")
            }
            testResult != null -> {
                println("❌ TEST 2 FAILED: $testResult")
            }
            else -> {
                println("❓ TEST 2 INCONCLUSIVE: Unknown state")
            }
        }

        // Check instance status
        println("📊 Instance status:")
        println("   - Ready: ${featurevisor.statuses.ready}")
        println("   - Refresh in progress: ${featurevisor.statuses.refreshInProgress}")
        println("   - Revision: ${featurevisor.getRevision()}")

    } catch (e: Exception) {
        println("💣 TEST 2 EXCEPTION: ${e.javaClass.simpleName}")
        println("   Message: ${e.message}")
        println("   Stack trace:")
        e.printStackTrace()
    }
}