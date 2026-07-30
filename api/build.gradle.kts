plugins {
    id("kotlinSetup")
}

dependencies {
    api(libs.ktor.client.core)
    api(libs.ktor.client.auth)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.serialization.json)
    api(libs.ktor.client.logging)

    testFixturesApi(libs.ktor.server.core)
    testFixturesApi(libs.ktor.server.content.negotiation)
}

kotlin.compilerOptions {
    freeCompilerArgs.add("-Xcontext-sensitive-resolution")
}

testing {
    suites {
        named("test", JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(projects.runtime))
                implementation(libs.groovy.all)

                implementation(libs.ktor.server.test.host)
                implementation(testFixtures(project()))
            }
        }
        register("sandboxTest", JvmTestSuite::class) {
            dependencies {
                runtimeOnly(libs.logback)
                implementation(testFixtures(project()))
                implementation(projects.runtime)

                implementation(libs.sapci.script.api)
                implementation(libs.sapci.adapter.api)
                implementation(testFixtures(projects.flowDsl))
                implementation(testFixtures(projects.proxyDsl))
                implementation(libs.ktor.client.cio)
            }

            targets.configureEach {
                testTask {
                    javaLauncher.set(javaToolchains.launcherFor {})
                    environment("SBX_API_CLIENT_SECRET", providers.gradleProperty("SBX_API_CLIENT_SECRET").get())
                    environment("SBX_RT_CLIENT_SECRET", providers.gradleProperty("SBX_RT_CLIENT_SECRET").get())
                }
            }
        }
    }
}
