plugins {
    id("kotlinSetup")
}

dependencies {
    api(projects.proxy)
}

kotlin.compilerOptions {
    optIn.add("kotlin.uuid.ExperimentalUuidApi")

    freeCompilerArgs.add("-Xcontext-sensitive-resolution")
}

testing.suites.named("test", JvmTestSuite::class) {
    dependencies {
        implementation(testFixtures(projects.api))
    }
}
