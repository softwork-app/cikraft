plugins {
    `kotlin-dsl`
    id("setup")
    id("java-test-fixtures")
    id("jvm-test-suite")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    compileOnly(libs.kotlin.ecosystem)
    compileOnly(libs.plugins.kotlin.serialization.dep)
    compileOnly(libs.plugins.ksp.dep)
    implementation(libs.r8.feature)
    implementation(projects.gradleWorker)

    compileOnly(projects.generator)
    compileOnly(projects.api)
    compileOnly(libs.ktor.client.cio)
    compileOnly(libs.ktor.client.logging)
    compileOnly(projects.integrationFlowBuilderRuntime)

    testFixturesCompileOnly(projects.runtime)
    testFixturesCompileOnly(projects.core)
    testFixturesCompileOnly(projects.integrationFlowBuilderRuntime)
    testFixturesCompileOnly(projects.ktorServerRuntime)
    testFixturesCompileOnly(libs.ktor.server.core)
    testFixturesCompileOnly(libs.ktor.server.resources)
    testFixturesImplementation(libs.serialization.json)
}

val Provider<PluginDependency>.dep: Provider<String> get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

val version = tasks.register("createVersion", VersionTask::class) {
    ktorCio.set(libs.ktor.client.cio.map { it.toString() })
    ktorLogging.set(libs.ktor.client.logging.map { it.toString() })
    sapCIScriptApi.set(libs.sapci.script.api.map { it.toString() })
    sapCIGeneric.set(libs.sapci.generic.api.map { it.toString() })
    sapCIAdapter.set(libs.sapci.adapter.api.map { it.toString() })
    camel.set(libs.apache.camel.map { it.toString() })
    activation.set(libs.sapci.javax.activation.map { it.toString() })
    groovy.set(libs.groovy.all.map { it.toString() })
}

sourceSets.main {
    kotlin.srcDir(version)
}

gradlePlugin {
    plugins.configureEach {
        displayName = "Gradle plugin to configure ksp for SAPCI entrypoint generation"
        description = "Gradle plugin to configure ksp for SAPCI entrypoint generation"
    }

    plugins.register("app.softwork.cikraft.ecosystem") {
        implementationClass = "app.softwork.cikraft.gradle.SAPCIEcosystemPlugin"
    }
    plugins.register("app.softwork.cikraft.openapi") {
        implementationClass = "app.softwork.cikraft.gradle.OpenApiPlugin"
    }
}

configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradleVersion.version("9.6").version)
        )
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

testing.suites {
    named("test", JvmTestSuite::class) {
        useKotlinTest()
    }
    register("integrationTest", JvmTestSuite::class) {
        useKotlinTest()

        dependencies {
            implementation(testFixtures(project()))
            implementation(libs.serialization.json)
            implementation(projects.generator)
            implementation(testFixtures(projects.generator))
            implementation(gradleTestKit())
            implementation(libs.ktor.server.cio)
            implementation(testFixtures(projects.api))
        }
        gradlePlugin.testSourceSet(sources)

        targets.configureEach {
            val isOffline = gradle.startParameter.isOffline
            testTask {
                val isDebugEnabled = providers.environmentVariable("DEBUGGER_ENABLED").map {
                    it.toBoolean()
                }.getOrElse(false)
                environment("DEBUGGER_ENABLED", isDebugEnabled)

                environment("fixtureDir", project.file("src/testFixtures").path)

                environment("offlineMode", isOffline)
            }
        }
    }

    register("functionalTest", JvmTestSuite::class) {
        useKotlinTest()

        dependencies {
            implementation(projects.api)
            implementation(testFixtures(project()))
            implementation(gradleTestKit())

            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        gradlePlugin.testSourceSet(sources)

        targets.configureEach {
            testTask {
                environment("fixtureDir", project.file("src/testFixtures").path)

                environment("SBX_API_CLIENT_SECRET", providers.gradleProperty("SBX_API_CLIENT_SECRET").get())
                environment("SBX_RT_CLIENT_SECRET", providers.gradleProperty("SBX_RT_CLIENT_SECRET").get())
                environment("KDGP_USERNAME", providers.gradleProperty("KDGPUsername").get())
                environment("KDGP_PASSWORD", providers.gradleProperty("KDGPPassword").get())
            }
        }
    }
}
