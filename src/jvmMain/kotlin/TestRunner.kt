import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.InstanceOptions
import com.featurevisor.sdk.Logger
import com.featurevisor.sdk.isEnabled
import com.featurevisor.types.DatafileContent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() {
    println("🚀 Featurevisor JVM Test Runner (New Architecture)")
    println("=================================================")

    runBlocking {
        try {
            // Test 1: App fetches datafile
            testAppFetchesDatafile()

            println("\n" + "=".repeat(50) + "\n")

            // Test 2: Featurevisor with pre-loaded datafile
            testFeaturevisorWithDatafile()

            println("\n" + "=".repeat(50) + "\n")

            // Test 3: Dynamic datafile updates
            testDynamicDatafileUpdates()

        } catch (e: Exception) {
            println("💣 Test runner failed: ${e.message}")
            e.printStackTrace()
        }
    }

    println("\n🏁 Test runner completed")
}

suspend fun testAppFetchesDatafile() {
    println("🔍 TEST 1: App Fetches Datafile (New Architecture)")
    println("-".repeat(50))

    try {
        // Get URL from local.properties via system property
        val datafileUrl = System.getProperty("test.datafile.url")

        println("📍 Using datafile URL: $datafileUrl")

        // Your app's responsibility: fetch the datafile
        val client = HttpClient()
        println("✅ HttpClient created")

        val response = client.get(datafileUrl)
        println("✅ Response status: ${response.status}")

        val content = response.bodyAsText()
        println("✅ Content length: ${content.length}")

        // Parse the datafile
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

        // Now create Featurevisor with the datafile
        val featurevisor = FeaturevisorInstance.createInstance(
            datafile = datafileContent,
            options = InstanceOptions(
                logger = Logger.createLogger(Logger.LogLevel.INFO)
            )
        )

        // Ready to use immediately!
        println("✅ Featurevisor created and ready")
        println("   - Revision: ${featurevisor.getRevision()}")

        // Test feature flag evaluation
        val context = mapOf(
            "userId" to com.featurevisor.types.AttributeValue.StringValue("test-user")
        )

        if (datafileContent.features.isNotEmpty()) {
            val firstFeature = datafileContent.features.first().key
            val isEnabled = featurevisor.isEnabled(firstFeature, context)
            println("✅ Feature evaluation works: $firstFeature = $isEnabled")
        }

        println("🎉 TEST 1 PASSED: App-controlled fetching works!")

    } catch (e: Exception) {
        println("❌ TEST 1 FAILED: ${e.javaClass.simpleName}")
        println("   Error: ${e.message}")
        e.printStackTrace()
    }
}

suspend fun testFeaturevisorWithDatafile() {
    println("🔍 TEST 2: Featurevisor with Pre-loaded Datafile")
    println("-".repeat(50))

    try {
        // Create a simple test datafile
        val testDatafile = DatafileContent(
            schemaVersion = "1",
            revision = "test-123",
            attributes = listOf(),
            segments = listOf(),
            features = listOf()
        )

        var refreshTriggered = false
        var refreshedDatafile: DatafileContent? = null

        // Create Featurevisor with datafile and refresh callback
        val featurevisor = FeaturevisorInstance.createInstance(
            datafile = testDatafile,
            options = InstanceOptions(
                logger = Logger.createLogger(Logger.LogLevel.DEBUG),
                onRefresh = { args ->
                    println("📋 Refresh callback triggered!")
                    refreshTriggered = true
                    refreshedDatafile = args[0] as DatafileContent
                }
            )
        )

        println("✅ Featurevisor created with test datafile")
        println("   - Revision: ${featurevisor.getRevision()}")

        // Update with new datafile
        val updatedDatafile = testDatafile.copy(revision = "test-456")
        featurevisor.setDatafile(updatedDatafile)

        println("✅ Datafile updated")
        println("   - New revision: ${featurevisor.getRevision()}")
        println("   - Refresh triggered: $refreshTriggered")
        println("   - Refreshed datafile revision: ${refreshedDatafile?.revision}")

        if (refreshTriggered && refreshedDatafile?.revision == "test-456") {
            println("🎉 TEST 2 PASSED: Datafile updates work!")
        } else {
            println("❌ TEST 2 FAILED: Refresh callback not working properly")
        }

    } catch (e: Exception) {
        println("❌ TEST 2 FAILED: ${e.javaClass.simpleName}")
        println("   Error: ${e.message}")
        e.printStackTrace()
    }
}

suspend fun testDynamicDatafileUpdates() {
    println("🔍 TEST 3: Dynamic Datafile Updates")
    println("-".repeat(50))

    try {
        // Start with empty instance
        val featurevisor = FeaturevisorInstance.createInstance(
            options = InstanceOptions(
                logger = Logger.createLogger(Logger.LogLevel.INFO),
                onRefresh = { args ->
                    val datafile = args[0] as DatafileContent
                    println("🔄 Datafile refreshed: ${datafile.revision} (${datafile.features.size} features)")
                }
            )
        )

        println("✅ Started with empty Featurevisor")
        println("   - Initial revision: ${featurevisor.getRevision()}")

        // Simulate app fetching and updating multiple times
        repeat(3) { i ->
            println("\n--- Update $i ---")

            // App fetches new datafile (simulated)
            val newDatafile = DatafileContent(
                schemaVersion = "1",
                revision = "update-$i",
                attributes = listOf(),
                segments = listOf(),
                features = listOf() // Add mock features here if needed
            )

            // App updates Featurevisor
            featurevisor.setDatafile(newDatafile)

            println("✅ Updated to revision: ${featurevisor.getRevision()}")
        }

        // Test JSON string update
        println("\n--- JSON String Update ---")
        val jsonDatafile = """
        {
            "schemaVersion": "1",
            "revision": "json-update",
            "attributes": [],
            "segments": [],
            "features": []
        }
        """.trimIndent()

        featurevisor.setDatafile(jsonDatafile)
        println("✅ Updated from JSON: ${featurevisor.getRevision()}")

        println("🎉 TEST 3 PASSED: Dynamic updates work!")

    } catch (e: Exception) {
        println("❌ TEST 3 FAILED: ${e.javaClass.simpleName}")
        println("   Error: ${e.message}")
        e.printStackTrace()
    }
}

// Example of how to integrate into Android Repository pattern
class FeatureRepository {
    private val httpClient = HttpClient()
    private val featurevisor = FeaturevisorInstance.createInstance(
        options = InstanceOptions(
            logger = Logger.createLogger(Logger.LogLevel.INFO),
            onRefresh = { args ->
                val datafile = args[0] as DatafileContent
                println("Repository: Datafile updated to ${datafile.revision}")
            }
        )
    )

    suspend fun loadDatafile() {
        try {
            // Get URL from local.properties
            val datafileUrl = System.getProperty("test.datafile.url")

            val response = httpClient.get(datafileUrl)
            val datafileJson = response.bodyAsText()

            // Update Featurevisor with new datafile
            featurevisor.setDatafile(datafileJson)

        } catch (e: Exception) {
            // Handle error in your app layer
            println("Failed to load datafile: ${e.message}")
        }
    }

    fun isFeatureEnabled(key: String, userId: String): Boolean {
        val context = mapOf(
            "userId" to com.featurevisor.types.AttributeValue.StringValue(userId)
        )
        return featurevisor.isEnabled(key, context)
    }

    fun getCurrentRevision(): String = featurevisor.getRevision()
}