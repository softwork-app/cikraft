plugins {
    id("kotlinSetup")
    id("groovy")
}

dependencies {
    api(libs.serialization.json)
    api(libs.kotlinpoet)
    api(libs.kfx.kotlin)
    api(projects.core)
    api(libs.kfx.openapi.model)

    testFixturesApi(libs.groovy.all)
    testFixturesApi(projects.runtime)
    testFixturesApi(libs.ktor.server.core)
    testFixturesApi(libs.ktor.server.resources)
    testFixturesApi(projects.ktorServerRuntime)
    testFixturesApi(libs.ktor.client.core)
    testFixturesApi(libs.ktor.client.resources)
    testFixturesApi(libs.ktor.client.content.negotiation)
    testFixturesApi(libs.ktor.serialization.kotlinx.json)
    testFixturesApi(libs.ktor.server.content.negotiation)

    testFixturesApi(testFixtures(projects.runtime))
    testFixturesApi(projects.integrationFlowBuilderRuntime)
}

testing.suites.register("integrationTest", JvmTestSuite::class) {
    dependencies {
        implementation(testFixtures(project()))

        implementation(libs.ktor.server.test.host)
        implementation(libs.ktor.client.logging)
    }
}

tasks.compileTestFixturesGroovy {
    classpath += files(tasks.compileTestFixturesKotlin)
}
