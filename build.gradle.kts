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
version = "1.0-SNAPSHOT"

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

    val hostOs = System.getProperty("os.name")
    val isMacosX64 = hostOs == "Mac OS X"
    val isMacosArm64 = hostOs == "Mac OS X" && System.getProperty("os.arch") == "aarch64"

    if (isMacosX64 || isMacosArm64) {
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
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
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

        if (isMacosX64 || isMacosArm64) {
            iosMain.dependencies {
                implementation("io.ktor:ktor-client-darwin:2.3.7")
            }
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
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tomasz-mancewicz/featurevisor-kotlin-multiplatform")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])

            groupId = "com.github.tomasz-mancewicz"
            artifactId = "featurevisor-kotlin-multiplatform"
            version = project.version.toString()

            pom {
                name.set("Featurevisor Kotlin Multiplatform")
                description.set("Kotlin Multiplatform SDK for Featurevisor")
                url.set("https://github.com/tomasz-mancewicz/featurevisor-kotlin-multiplatform")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("tomasz-mancewicz")
                        name.set("Tomasz Mancewicz")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/tomasz-mancewicz/featurevisor-kotlin-multiplatform.git")
                    developerConnection.set("scm:git:ssh://github.com/tomasz-mancewicz/featurevisor-kotlin-multiplatform.git")
                    url.set("https://github.com/tomasz-mancewicz/featurevisor-kotlin-multiplatform")
                }
            }
        }
    }
}