# Featurevisor Kotlin Multiplatform

A complete Kotlin Multiplatform SDK for [Featurevisor](https://featurevisor.com) - feature flags, experiments, and remote config management.

> **Note**: This is an independent implementation forked and significantly extended from the original [featurevisor-kotlin](https://github.com/featurevisor/featurevisor-kotlin) repository. While the original was marked as "work in progress," this implementation provides a fully functional Kotlin Multiplatform solution with comprehensive feature parity.

## ✨ Features

- 🎯 **Full Feature Parity** - Complete implementation of Featurevisor SDK capabilities
- 🔄 **Kotlin Multiplatform** - Supports Android, iOS, JVM, and Native targets
- 🧪 **Testing Framework** - Built-in test runner for feature flag specifications
- 📊 **Benchmarking** - Performance measurement tools for feature evaluation
- 🔍 **Segment Evaluation** - Advanced user segmentation and targeting
- 🎛️ **Variable Management** - Support for feature variables with multiple data types
- 📱 **Mobile Ready** - Optimized for Android (API 21+) and iOS applications

## 🚀 Installation

### Using JitPack (Recommended)

Add JitPack repository to your project:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.tomasz-mancewicz:featurevisor-kotlin-multiplatform:main-SNAPSHOT")
    // or use a specific release:
    // implementation("com.github.tomasz-mancewicz:featurevisor-kotlin-multiplatform:v0.1.0")
}
```

### Platform-specific setup

**Android:**
```kotlin
androidMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
}
```

**iOS:**
```kotlin
iosMain.dependencies {
    implementation("io.ktor:ktor-client-darwin:2.3.7")
}
```

**JVM:**
```kotlin
jvmMain.dependencies {
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
}
```

## 📖 Usage

### Basic SDK Usage

```kotlin
import com.featurevisor.sdk.FeaturevisorInstance
import com.featurevisor.sdk.InstanceOptions

// Initialize the SDK
val sdk = FeaturevisorInstance.createInstance(
    InstanceOptions(
        datafile = datafileContent, // Your Featurevisor datafile
        onReady = { /* SDK ready callback */ }
    )
)

// Set user context
val context = mapOf(
    "userId" to AttributeValue.StringValue("user-123"),
    "country" to AttributeValue.StringValue("US"),
    "appVersion" to AttributeValue.StringValue("1.0.0")
)

// Evaluate feature flags
val isEnabled = sdk.isEnabled("myFeatureKey", context)
val variation = sdk.getVariation("myFeatureKey", context)
val variable = sdk.getVariable("myFeatureKey", "someVariableKey", context)
```

### Testing Features

This SDK includes a comprehensive testing framework:

```kotlin
import com.featurevisor.testRunner.startTest
import com.featurevisor.testRunner.TestProjectOption

// Run tests
startTest(
    TestProjectOption(
        projectRootPath = "/path/to/featurevisor/project",
        keyPattern = "feature-name",    // Filter by feature name
        verbose = true,                  // Detailed logging
        onlyFailures = false            // Show all results
    )
)
```

### Benchmarking

Measure feature evaluation performance:

```kotlin
import com.featurevisor.testRunner.benchmarkFeature
import com.featurevisor.testRunner.BenchMarkOptions

benchmarkFeature(
    BenchMarkOptions(
        environment = "production",
        feature = "myFeatureKey",
        n = 10000,                      // Number of iterations
        projectRootPath = "/path/to/project",
        context = context
    )
)
```

## 🏗️ Architecture

This implementation provides:

- **Core SDK** (`com.featurevisor.sdk`) - Feature evaluation engine
- **Test Runner** (`com.featurevisor.testRunner`) - Testing and benchmarking tools
- **Type System** (`com.featurevisor.types`) - Comprehensive type definitions
- **Serializers** (`com.featurevisor.sdk.serializers`) - Data serialization utilities

## 🎯 Platform Support

| Platform | Support | Notes |
|----------|---------|-------|
| Android | ✅ | API 21+ |
| iOS | ✅ | iOS 12+ |
| JVM | ✅ | Java 17+ |
| Native | ✅ | Linux, macOS, Windows |

## 🤝 Relationship to Original Project

This repository was forked from the original [featurevisor-kotlin](https://github.com/featurevisor/featurevisor-kotlin) project but has been:

- Completely rewritten and extended
- Made fully functional with comprehensive feature parity
- Enhanced with testing and benchmarking capabilities
- Optimized for production use

**Credits to the original Featurevisor team** for creating the excellent JavaScript SDK and ecosystem that inspired this implementation.

## 📚 Documentation

- [Featurevisor Official Documentation](https://featurevisor.com/docs/)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Testing Framework Guide](https://featurevisor.com/docs/testing/)

## 🔧 Development

### Building the project

```bash
./gradlew build
```

### Running tests

```bash
./gradlew test
```

### Publishing to local Maven

```bash
./gradlew publishToMavenLocal
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Featurevisor](https://github.com/featurevisor/featurevisor) - The original feature management platform
- [Featurevisor Kotlin](https://github.com/featurevisor/featurevisor-kotlin) - The initial Kotlin implementation that inspired this work
- Kotlin Multiplatform community for excellent tooling and ecosystem
