plugins {
    `kotlin-dsl`
    id("setup")
    id("java-test-fixtures")
    id("jvm-test-suite")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.kotlin.ecosystem)
    implementation(libs.jib.gradle.plugin)
    implementation(projects.gradlePlugin)

    compileOnly(projects.core)
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

gradlePlugin {
    plugins.configureEach {
        displayName = "Gradle plugin to configure ksp for SAPCI entrypoint generation"
        description = "Gradle plugin to configure ksp for SAPCI entrypoint generation"
    }

    plugins.register("app.softwork.cikraft.docker-compose-feature") {
        implementationClass = "app.softwork.cikraft.gradle.DockerComposeFeaturePlugin"
    }
    plugins.register("app.softwork.cikraft.docker-compose") {
        implementationClass = "app.softwork.cikraft.gradle.DockerComposePlugin"
    }
}

configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradleVersion.version("9.7").version)
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

                environment("KDGP_USERNAME", providers.gradleProperty("KDGPUsername").get())
                environment("KDGP_PASSWORD", providers.gradleProperty("KDGPPassword").get())
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
