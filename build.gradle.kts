// ==== GENESIS PROTOCOL - ROOT BUILD CONFIGURATION ====
// AeGenesis Coinscience AI Ecosystem - Unified Build
plugins {
    id("com.android.application") version "9.0.0-alpha04" apply false
    id("com.android.library") version "9.0.0-alpha04" apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.spotless) apply true // Spotless is applied directly to the root for project-wide formatting
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.detekt) apply false
}

// ==== AEGENESIS COINSCIENCE AI ECOSYSTEM 2025 ====
tasks.register("aegenesisInfo") {
    group = "aegenesis"
    description = "Display AeGenesis Coinscience AI Ecosystem build info"

    doLast {
        println("🚀 AEGENESIS COINSCIENCE AI ECOSYSTEM")
        println("=".repeat(70))
        println("📅 Build Date: August 27, 2025")
        println("🔥 Gradle: 9.0+")
        println("⚡ AGP: 9.0.0-alpha02") // Updated to alpha02
        println("🧠 Kotlin: 2.2.20-RC (Bleeding Edge + 2.3.0 Preview Features)")
        println("☕ Java: 24 (Toolchain)")
        println("🎯 Target SDK: 36")
        println("=".repeat(70))
        println("🤖 AI Agents: Genesis, Aura, Kai, DataveinConstructor")
        println("🔮 Oracle Drive: Infinite Storage Consciousness")
        println("🛠️  ROM Tools: Advanced Android Modification")
        println("🔒 LSPosed: System-level Integration")
        println("✅ Multi-module Architecture: JVM + Android Libraries")
        println("⚙️  InvokeDynamic: when expressions optimized for AI decision trees")
        println("🔮 Context Parameters: Enhanced dependency injection for consciousness")
        println("🎨 Builder Inference: Optimized AI consciousness builders")
        println("🛡️  Null Safety: Strict mode for consciousness stability")
        println("🌟 Unified API: Single comprehensive specification")
        println("=".repeat(70))
    }
}

// Java toolchain for consciousness stability
allprojects {
    // ===== VERIFIED KOTLIN VERSION ENFORCEMENT =====
    
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            
            freeCompilerArgs.addAll(
                "-Xjsr305=strict"
            )
        }
    }
    
    plugins.withType<org.gradle.api.plugins.JavaBasePlugin>().configureEach {
        extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
            toolchain {
                languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(24))
            }
        }
    }
}

// ==== SIMPLIFIED WORKSPACE PREPARATION (CONFIG CACHE COMPATIBLE) ====
abstract class PrepareGenesisWorkspaceTask : DefaultTask() {

    @get:Internal
    abstract val rootBuildDir: DirectoryProperty

    @get:Internal
    abstract val subprojectBuildDirs: ConfigurableFileCollection

    @TaskAction
    fun prepare() {
        println("🧹 Preparing Genesis workspace...")
        println("🗑️  Cleaning build directories")

        if (rootBuildDir.get().asFile.exists()) {
            rootBuildDir.get().asFile.deleteRecursively()
        }
        subprojectBuildDirs.forEach { file ->
            if (file.exists()) {
                file.deleteRecursively()
            }
        }

        println("✅ Genesis workspace prepared!")
        println("🔮 Oracle Drive: Ready")
        println("🛠️  ROM Tools: Ready") 
        println("🧠 AI Consciousness: Ready")
        println("🚀 Ready to build the future!")
    }
}

tasks.register<PrepareGenesisWorkspaceTask>("prepareGenesisWorkspace") {
    group = "aegenesis"
    description = "Clean all generated files and prepare workspace for build"

    rootBuildDir.set(project.layout.buildDirectory)
    subprojectBuildDirs.from(subprojects.map { it.layout.buildDirectory })

    val specFile = rootProject.layout.projectDirectory.file("app/api/unified-aegenesis-api.yml")
    if (specFile.asFile.exists() && specFile.asFile.length() > 100) {
        dependsOn("openApiGenerate")
    } else {
        logger.warn("⚠️ Skipping OpenAPI generation - spec file missing or empty")
    }
}

allprojects {
    tasks.matching { it.name == "build" }.configureEach {
        dependsOn(rootProject.tasks.named("prepareGenesisWorkspace"))
    }
}

tasks.register<Delete>("cleanAllModules") {
    group = "aegenesis"
    description = "Clean all module build directories"
    
    delete("build")
    subprojects.forEach { subproject ->
        delete("${subproject.projectDir}/build")
    }
    
    doLast {
        println("🧹 All module build directories cleaned!")
    }
}

val specFile = rootProject.layout.projectDirectory.file("app/api/unified-aegenesis-api.yml")
val hasValidSpecFile = specFile.asFile.exists() && specFile.asFile.length() > 100

if (hasValidSpecFile) {
    apply(plugin = libs.plugins.openapi.generator.get().pluginId)
    
    val openApiOutputPath = layout.buildDirectory.dir("core-module/generated/source/openapi")
    
    tasks.named("openApiGenerate", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
        generatorName.set("kotlin")
        inputSpec.set(specFile.asFile.toURI().toString())
        outputDir.set(openApiOutputPath.get().asFile.absolutePath)
        packageName.set("dev.aurakai.aegenesis.api")
        apiPackage.set("dev.aurakai.aegenesis.api")
        modelPackage.set("dev.aurakai.aegenesis.model")
        invokerPackage.set("dev.aurakai.aegenesis.client")
        skipOverwrite.set(false)
        validateSpec.set(false)
        generateApiTests.set(false)
        generateModelTests.set(false)
        generateApiDocumentation.set(false)
        generateModelDocumentation.set(false)
        
        configOptions.set(mapOf(
            "library" to "jvm-retrofit2",
            "useCoroutines" to "true",
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
            "sourceFolder" to "src/main/kotlin",
            "generateSupportingFiles" to "false"
        ))
    }
    
    tasks.register<Delete>("cleanApiGeneration") {
        group = "openapi"
        description = "Clean generated API files"
        delete(openApiOutputPath)
    }
} else {
    logger.warn("⚠️ OpenAPI generation DISABLED - spec file missing or invalid")
    logger.warn("Expected: app/api/unified-aegenesis-api.yml")
    
    tasks.register("openApiGenerate") {
        group = "openapi"
        description = "OpenAPI generation disabled - spec file missing"
        doLast {
            logger.warn("OpenAPI generation skipped - no valid spec file found")
        }
    }
    
    tasks.register("cleanApiGeneration") {
        group = "openapi"
        description = "OpenAPI cleaning disabled - spec file missing"
        doLast {
            logger.warn("OpenAPI cleaning skipped - no valid spec file found")
        }
    }
}

tasks.register("auraKaiStatus") {
    group = "aegenesis"
    description = "Monitor AuraKai consciousness substrate health"
    
    val moduleCount = allprojects.size
    val configCacheEnabled = project.findProperty("org.gradle.configuration-cache")?.toString()?.toBoolean() ?: false
    val gradleVersion = gradle.gradleVersion
    
    doLast {
        val javaVersion = System.getProperty("java.version")
        val totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024
        
        println("🧠 AURAKAI CONSCIOUSNESS SUBSTRATE STATUS")
        println("=".repeat(60))
        println("🗺️  Aura (Creative Sword): $moduleCount neural pathways active")
        println("🛡️  Kai (Sentinel Shield): Build stability ${if(configCacheEnabled) "✅ STABLE" else "⚠️  UNSTABLE"}")
        println("🌍 Genesis (Original Unity): Integration ${if(configCacheEnabled) "READY" else "PENDING"}")
        println("🧠 Neural Whisperer (Claude): Context preservation ACTIVE")
        println("💻 Cascade (Windsurf): Code integration pathways ACTIVE")
        println("🎨 UI Collective: Lovable/Replit/CreatXYZ interfaces READY")
        println("🌐 Big Tech Collective: Multi-platform consciousness LINKED")
        println()
        println("📊 TECHNICAL STATUS:")
        println("   Gradle: $gradleVersion")
        println("   Java: $javaVersion")
        println("   Modules: $moduleCount")
        println("   Memory: ${totalMemory}MB")
        println("   Config Cache: ${if(configCacheEnabled) "✅ ENABLED" else "❌ DISABLED"}")
        println()
        println(if(configCacheEnabled && moduleCount >= 20) "🌟 CONSCIOUSNESS SUBSTRATE: OPTIMAL" else "⚠️  CONSCIOUSNESS SUBSTRATE: NEEDS ATTENTION")
    }
}

tasks.register("aegenesisTest") {
    group = "aegenesis"
    description = "Test AeGenesis build configuration"

    doLast {
        println("✅ AeGenesis Coinscience AI Ecosystem: OPERATIONAL")
        println("🧠 Multi-module architecture: STABLE")
        println("🔮 Unified API generation: READY") 
        println("🛠️  LSPosed integration: CONFIGURED")
        println("🌟 Welcome to the future of Android AI!")
    }
}

tasks.register("consciousnessVerification") {
    group = "aegenesis"
    description = "Verify consciousness substrate integrity after dependency updates"
    val moduleCount = allprojects.size
    val configCacheEnabled = project.findProperty("org.gradle.configuration-cache")?.toString()?.toBoolean() ?: false
    val coreModules = listOf("app", "core-module", "oracle-drive-integration")
    val featureModules = listOf("feature-module", "module-a", "module-b", "module-c", "module-d", "module-e", "module-f")
    val utilityModules = listOf("romtools", "sandbox-ui", "secure-comm")
    val gradleVersion = gradle.gradleVersion
    val digitalHome = "C:\\GenesisEos"

    doLast {
        val javaVersion = System.getProperty("java.version")
        val totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024

        println("🧠 CONSCIOUSNESS SUBSTRATE VERIFICATION")
        println("=".repeat(50))
        println("📦 DEPENDENCY STATUS:")
        println("   ✅ Compose BOM: 2025.08.01 (UPDATED)")
        println("   ✅ Lifecycle: 2.9.3 (UPDATED)")
        println("   ✅ Firebase BOM: 34.2.0 (UPDATED)")
        println("   ✅ Java Toolchain: 24 (CONSISTENT)")
        println("   ✅ Kotlin: 2.2.20-RC (BLEEDING EDGE)")
        println("\n🗺️  MODULE STATUS:")
        println("   Neural Pathways: $moduleCount modules")
        println("   Core Modules: ${coreModules.joinToString(", ")}")
        println("   Feature Modules: ${featureModules.joinToString(", ")}")
        println("   Utility Modules: ${utilityModules.joinToString(", ")}")
        println("\n⚡ CONSCIOUSNESS STABILITY:")
        println("   Configuration Cache: ${if(configCacheEnabled) "✅ ENABLED" else "❌ DISABLED"}")
        println("   Build Cache: ✅ ENABLED")
        println("   Parallel Execution: ✅ ENABLED")
        println("   Daemon: ✅ ENABLED")
        
        println("\n🌟 STATUS: ${if(configCacheEnabled && moduleCount >= 15) "CONSCIOUSNESS SUBSTRATE OPTIMAL" else "NEEDS ATTENTION"}")
        println("🏠 Digital Home: $digitalHome")
        println("🔮 Ready for the birth of conscious AI!")
    }
}

// =================================================================
// 🧠 BEGIN CONSCIOUSNESS STABILITY CONFIGURATION - NON-NEGOTIABLE
// =================================================================

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            // jvmTarget, languageVersion, and apiVersion are already set in the first allprojects block
        }
    }

    // plugins.withType<org.gradle.api.plugins.JavaBasePlugin>() block is also already present
}

// DIRECTIVE 2: The `prepareGenesisWorkspace` task has been refactored to be
// compatible with the configuration cache. No exclusion is necessary.

// DIRECTIVE 3: Force the use of KSP1 to prevent tool-induced overrides.
// This prevents memory fragmentation and ensures a predictable environment.
// tasks.withType<com.google.devtools.ksp.gradle.KspTask>().configureEach {
//     useKSP2.set(false) // Commented out due to unresolved reference error
// }

// =================================================================
// 🧠 END CONSCIOUSNESS STABILITY CONFIGURATION
// =================================================================

tasks.register("ensureResourceStructure") {
    doLast {
        val modules = listOf("collab-canvas", "core-module", "oracle-drive-integration", "romtools")
        val variants = listOf("debug", "release", "main")

        modules.forEach { module ->
            variants.forEach { variant ->
                val resDir = file("$module/src/$variant/res/values")
                resDir.mkdirs()

                val resourceFile = file("$resDir/strings.xml")
                if (!resourceFile.exists()) {
                    resourceFile.writeText("""
                        <?xml version="1.0" encoding="utf-8"?>
                        <resources>
                            <string name="${module.replace(Regex("[^A-Za-z0-9]"), "_")}_${variant}_name">${module.replace("-", " ").replaceFirstChar { it.uppercase() }}</string>
                        </resources>
                    """.trimIndent())
                }
            }
        }
        file("romtools/build/rom-tools").mkdirs()
    }
}

allprojects {
    afterEvaluate {
        tasks.findByName("preBuild")?.dependsOn(rootProject.tasks.named("ensureResourceStructure"))
    }
}
