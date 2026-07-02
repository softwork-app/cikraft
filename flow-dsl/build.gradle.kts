plugins {
    id("kotlinSetup")
}

dependencies {
    api(libs.serialization.xml)
    api(libs.serialization.properties)
}

testing.suites.named("test", JvmTestSuite::class) {
    dependencies {
        implementation(testFixtures(projects.api))
    }
}
