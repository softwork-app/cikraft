plugins {
    id("kotlinSetup")
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(projects.core)
    compileOnly(projects.integrationFlowBuilderRuntime)

    compileOnly(projects.api)
    compileOnly(libs.ktor.client.cio)
    compileOnly(libs.ktor.client.logging)
}
