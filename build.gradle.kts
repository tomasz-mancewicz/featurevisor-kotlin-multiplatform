import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    kotlin("plugin.allopen") version "2.2.0"
    id("com.android.library") version "8.10.1"
    id("dev.mokkery") version "2.9.0"
    id("maven-publish")
}

group = "com.featurevisor"
version = "0.1.0"

kotlin {

    jvmToolchain(17)

    // JVM target
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Android target
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release", "debug")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FeaturevisorSDK"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            implementation("com.goncalossilva:murmurhash:0.4.1")
            implementation("net.swiftzer.semver:semver:2.1.0")
            // Ktor for HTTP client (multiplatform)
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.kotest:kotest-assertions-core:5.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
        }


        jvmMain.dependencies {
            // Ktor JVM engine
            implementation("io.ktor:ktor-client-okhttp:2.3.7")
            // Test runner dependencies (JVM-only)
            implementation("org.yaml:snakeyaml:2.2")
            implementation("com.google.code.gson:gson:2.10.1")
        }

        jvmTest.dependencies {
            implementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
            implementation("io.mockk:mockk:1.13.8")
            implementation("io.kotest:kotest-assertions-core:5.8.0")
        }

        androidMain.dependencies {
            // Ktor Android engine
            implementation("io.ktor:ktor-client-okhttp:2.3.7")
        }

        iosMain.dependencies {
            // Ktor iOS engine
            implementation("io.ktor:ktor-client-darwin:2.3.7")
        }
    }
}

android {
    namespace = "com.featurevisor.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

allOpen {
    annotation("com.featurevisor.utils.OpenForMokkery")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])

            groupId = "com.featurevisor"
            artifactId = "featurevisor-kotlin-multiplatform"
            version = project.version.toString()
        }
    }
}