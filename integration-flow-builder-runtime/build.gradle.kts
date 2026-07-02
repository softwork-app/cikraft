plugins {
    id("kotlinSetup")
}

dependencies {
    api(projects.flowDsl)
    api(projects.proxyDsl)

    implementation(projects.core)
}
