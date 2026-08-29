plugins {
    id("kotlinSetup")
}

dependencies {
    api(projects.proxyDsl)

    implementation(projects.core)

    testImplementation(testFixtures(projects.generator))
}
