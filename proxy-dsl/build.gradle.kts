plugins {
    id("kotlinSetup")
}

dependencies {
    api(projects.proxy)
}

kotlin.compilerOptions {
    freeCompilerArgs.add("-Xcontext-sensitive-resolution")
}

testing.suites.named("test", JvmTestSuite::class) {
    dependencies {
        implementation(testFixtures(projects.api))
    }
}
