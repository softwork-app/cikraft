plugins {
    id("kotlinSetup")
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.sapci.script.api)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.server.resources)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.server.content.negotiation)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(testFixtures(projects.runtime))
}
