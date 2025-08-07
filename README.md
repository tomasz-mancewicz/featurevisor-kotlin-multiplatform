
# Featurevisor Kotlin Multiplatform

A complete Kotlin Multiplatform SDK for [Featurevisor](https://featurevisor.com) - feature flags, experiments, and remote config management.

> **Note**: This is an independent implementation forked and extended from the original [featurevisor-kotlin](https://github.com/featurevisor/featurevisor-kotlin) repository. This implementation provides a fully functional Kotlin Multiplatform solution.

## ✨ Features

- 🎯 **Full Feature Parity** - Complete implementation of Featurevisor SDK capabilities
- 🔄 **Kotlin Multiplatform** - Supports Android, iOS, JVM, and Native targets
- 🧪 **Testing Framework** - Built-in test runner for feature flag specifications
- 📊 **Benchmarking** - Performance measurement tools for feature evaluation
- 🔍 **Segment Evaluation** - Advanced user segmentation and targeting
- 🎛️ **Variable Management** - Support for feature variables with multiple data types
- 📱 **Mobile Ready** - Optimized for Android (API 21+) and iOS applications

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

- Platform speciffic parts rewritten and extended
- Updated dependencies for multiplatform support

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
