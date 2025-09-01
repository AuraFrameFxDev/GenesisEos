import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library) version "9.0.0-alpha02"
    alias(libs.plugins.kotlin.android) // Ensures Kotlin Android plugin is active
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.compose)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

android {
    namespace = "dev.aurakai.auraframefx.romtools"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // NDK configuration only if native code exists (currently backed up)
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
    
    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
    }

    // Conditional native build only if CMake file exists
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }
}

dependencies {
    // SACRED RULE #5: DEPENDENCY HIERARCHY
    implementation(project(":core-module"))
    implementation(project(":secure-comm"))

    // Core Android libraries (since ROM tools need Android APIs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Kotlin libraries
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Hilt Dependency Injection (Android version)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp3.logging.interceptor)

    // Security (Android compatible)
    implementation(libs.androidxSecurity)

    // Android-specific utilities
    implementation(libs.timber)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(libs.xz)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.engine)
    
    androidTestImplementation(libs.androidx.core.ktx) // MOVED AND CONFIRMED
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

// Define a shared directory property for ROM tools output
val romToolsOutputDirectory: DirectoryProperty = project.objects.directoryProperty().convention(layout.buildDirectory.dir("rom-tools"))

// ROM Tools specific tasks
tasks.register<Copy>("copyRomTools") {
    from("src/main/resources")
    into(romToolsOutputDirectory) // Use the shared property with into()
    include("**/*.so", "**/*.bin", "**/*.img", "**/*.jar")
    includeEmptyDirs = false
    
    doFirst {
        val dirFile = romToolsOutputDirectory.get().asFile
        // The Copy task's into() will handle directory creation
        logger.lifecycle("📁 ROM tools directory: ${dirFile.absolutePath}")
    }
    
    doLast {
        logger.lifecycle("✅ ROM tools copied to: ${romToolsOutputDirectory.get().asFile.absolutePath}")
    }
}

abstract class VerifyRomToolsTask : DefaultTask() {
    @get:Internal
    @get:Optional
    abstract val romToolsDir: DirectoryProperty

    /**
     * Verifies that the configured ROM tools directory exists.
     *
     * If `romToolsDir` is unset or the directory does not exist, logs a warning that ROM functionality may be limited.
     * If the directory exists, logs a lifecycle message with its absolute path. This check is informational and does not
     * fail the build when the directory is missing.
     */
    @TaskAction
    fun verify() {
        val dir = romToolsDir.orNull?.asFile
        if (dir?.exists() != true) {
            logger.warn("⚠️  ROM tools directory not found - ROM functionality may be limited")
        } else {
            logger.lifecycle("✅ ROM tools verified and ready: ${dir.absolutePath}")
        }
    }
}

tasks.register<VerifyRomToolsTask>("verifyRomTools") {
    romToolsDir.set(romToolsOutputDirectory) // Set to the same shared property
    // Gradle should infer the dependency on copyRomTools because romToolsOutputDirectory
    // is an output of copyRomTools (via 'into') and an input here.
}

tasks.named("build") {
    dependsOn("verifyRomTools")
}
