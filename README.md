# Featurevisor Kotlin Multiplatform SDK

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A **Kotlin Multiplatform** SDK for [Featurevisor](https://featurevisor.com) - a lightweight, fast, and flexible feature flag and A/B testing solution.

> **Note**: This is an independent implementation forked and extended from the original [featurevisor-kotlin](https://github.com/featurevisor/featurevisor-kotlin) repository. This implementation provides a fully functional Kotlin Multiplatform solution.
> 
## ✨ Features

- 🚀 **Lightweight & Fast** - No network dependencies, pure evaluation engine
- 🎯 **App-Controlled** - Your app handles datafile fetching, SDK handles evaluation
- 🔄 **Event-Driven** - Simple callback-based updates
- 🛡️ **Type-Safe** - Strong typing with sealed classes for context and variables
- 📱 **Multiplatform** - Android, iOS, JVM support
- 🎛️ **Hierarchical Logging** - Simple, efficient logging system
- ⚡ **High Performance** - Evaluate 300+ features in ~3ms
- 🔧 **V2 Compatible** - Aligned with latest Featurevisor architecture
- 📞 **Simple Callbacks** - Event-driven updates via callbacks

## 🏗️ Architecture

This SDK follows Featurevisor v2 principles:

- **Your app** handles datafile fetching (HTTP, caching, error handling)
- **SDK** focuses purely on feature flag evaluation
- **Event-driven** callbacks for datafile updates

## 📦 Installation

### Android/JVM
```kotlin
dependencies {
    implementation "com.github.tomasz-mancewicz:featurevisor-kotlin-multiplatform:1.0.0"
}
```

### iOS (Kotlin Multiplatform)
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.tomasz-mancewicz:featurevisor-kotlin-multiplatform:1.0.0")
        }
    }
}
```

## 🚀 Quick Start

### 1. Basic Setup

```kotlin
import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.InstanceOptions
import com.featurevisor.sdk.Logger

// Create instance
val featurevisor = FeaturevisorInstance.createInstance(
    options = InstanceOptions(
        onRefresh = { args ->
            val datafile = args[0] as DatafileContent
            println("✅ Features updated: ${datafile.features.size} features")
        },
        logger = Logger.createLogger(Logger.LogLevel.INFO)
    )
)
```

### Load Datafile (Your App's Responsibility)

```kotlin
// Your app fetches datafile however you want
suspend fun loadFeatures() {
    try {
        val datafileJson = httpClient.get("https://your-api.com/datafile").bodyAsText()
        featurevisor.setDatafile(datafileJson) // ✅ Ready to use immediately!
    } catch (e: Exception) {
        // Your app handles errors
        println("Failed to load features: ${e.message}")
    }
}
```

### Use Feature Flags

```kotlin
// Create user context
val context = mapOf(
    "userId" to AttributeValue.StringValue("user-123"),
    "country" to AttributeValue.StringValue("US"),
    "isPremium" to AttributeValue.BooleanValue(true)
)

// Check feature flags
val isNewUIEnabled = featurevisor.isEnabled("new_ui_design", context)
val buttonColor = featurevisor.getVariation("button_color_test", context)
val maxRetries = featurevisor.getVariableInteger("api_config", "max_retries", context)

// Use in your UI
if (isNewUIEnabled) {
    showNewUI()
} else {
    showOldUI()
}
```

### Hierarchical Logging

```kotlin
// Simple hierarchical logging - one level includes all below
val logger = Logger.createLogger(
    level = Logger.LogLevel.DEBUG, // Includes INFO, WARN, ERROR
    handle = { level, message, details ->
        when (level) {
            Logger.LogLevel.ERROR -> Log.e("Featurevisor", message)
            Logger.LogLevel.WARN -> Log.w("Featurevisor", message)
            Logger.LogLevel.INFO -> Log.i("Featurevisor", message)
            Logger.LogLevel.DEBUG -> Log.d("Featurevisor", message)
            Logger.LogLevel.NONE -> { /* Silent */ }
        }
    }
)

// Change level at runtime
logger.setLevel(Logger.LogLevel.WARN) // Now only shows WARN and ERROR
```

### Context Interception

```kotlin
val featurevisor = FeaturevisorInstance.createInstance(
    options = InstanceOptions(
        interceptContext = { context ->
            // Add default attributes to every evaluation
            context + mapOf(
                "timestamp" to AttributeValue.StringValue(Clock.System.now().toString()),
                "platform" to AttributeValue.StringValue("android"),
                "appVersion" to AttributeValue.StringValue(BuildConfig.VERSION_NAME)
            )
        }
    )
)
```

### Sticky Features (Override for Testing)

```kotlin
val stickyFeatures = mapOf(
    "beta_feature" to OverrideFeature(
        enabled = true,
        variation = "treatment",
        variables = mapOf(
            "color" to VariableValue.StringValue("blue")
        )
    )
)

val featurevisor = FeaturevisorInstance.createInstance(
    options = InstanceOptions(stickyFeatures = stickyFeatures)
)
```

## 🧪 Testing

### Unit Testing

```kotlin
@Test
fun `feature flags work with test datafile`() {
    val testDatafile = DatafileContent(
        schemaVersion = "1",
        revision = "test",
        attributes = emptyList(),
        segments = emptyList(),
        features = listOf(
            Feature(
                key = "test_feature",
                // ... feature configuration
            )
        )
    )
    
    val featurevisor = FeaturevisorInstance.createInstance()
    featurevisor.setDatafile(testDatafile)
    
    val context = mapOf("userId" to AttributeValue.StringValue("test-user"))
    val isEnabled = featurevisor.isEnabled("test_feature", context)
    
    assertTrue(isEnabled)
}
```

### Integration Testing

```kotlin
class FeatureRepositoryTest {
    @Test
    fun `should handle datafile updates`() = runTest {
        val repository = FeatureRepository(mockHttpClient, testScope)
        
        // Mock HTTP response
        mockHttpClient.mockResponse(datafileJson)
        
        repository.refreshFeatures()
        
        assertTrue(repository.isFeatureEnabled("test_feature", "user123"))
    }
}
```

## 📊 Performance

Benchmarks on typical feature flag workloads:

| Operation | Time | Notes |
|-----------|------|--------|
| Single flag evaluation | ~7.5μs | Simple flag check |
| Flag with variation | ~9μs | Includes variation resolution |
| Flag with variables | ~15μs | Includes variable resolution |
| **~400 flags evaluation** | **~3ms** | Full datafile resolution |
| Pre-resolved lookup | ~0.1μs | Instant access after resolution |

## 🔧 Configuration

### Local Properties (for testing)

```properties
# local.properties
test.datafile.url=https://your-api.com/staging/datafile.json
test.timeout.seconds=30
test.verbose=true
```

```kotlin
// build.gradle.kts
tasks.register<JavaExec>("runTest") {
    systemProperty("test.datafile.url", localProperties.getProperty("test.datafile.url", ""))
    // ... other properties
}
```

## 📚 API Reference

### Core Methods

```kotlin
// Feature flag evaluation
featurevisor.isEnabled(featureKey: String, context: Context): Boolean
featurevisor.getVariation(featureKey: String, context: Context): String?

// Variable access
featurevisor.getVariable(featureKey: String, variableKey: String, context: Context): VariableValue?
featurevisor.getVariableBoolean(featureKey: String, variableKey: String, context: Context): Boolean?
featurevisor.getVariableString(featureKey: String, variableKey: String, context: Context): String?
featurevisor.getVariableInteger(featureKey: String, variableKey: String, context: Context): Int?

// Datafile management
featurevisor.setDatafile(datafileContent: DatafileContent)
featurevisor.setDatafile(datafileJSON: String)
featurevisor.getRevision(): String
```

### Context Types

```kotlin
// Strong-typed context attributes
AttributeValue.StringValue(value: String)
AttributeValue.IntValue(value: Int)
AttributeValue.DoubleValue(value: Double)
AttributeValue.BooleanValue(value: Boolean)
AttributeValue.DateValue(value: Instant)
```

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Links

- [Featurevisor Documentation](https://featurevisor.com)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [GitHub Repository](https://github.com/tomasz-mancewicz/featurevisor-kotlin-multiplatform)

## 📋 Changelog

### v1.0.0
- ✅ Complete Featurevisor v2 compatibility
- ✅ Removed network dependencies (app-controlled fetching)
- ✅ Hierarchical logging system
- ✅ Strong-typed context with AttributeValue
- ✅ High-performance evaluation engine
- ✅ Comprehensive test suite with JVM testRunner to call your fv endpoint

---

Made with ❤️ for the Kotlin community
