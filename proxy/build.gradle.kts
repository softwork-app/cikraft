plugins {
    id("kotlinSetup")
}

dependencies {
    implementation(libs.serialization.xml)
    api(libs.kotlinx.io.bytestring)
}

kotlin.compilerOptions {
    optIn.add("kotlin.uuid.ExperimentalUuidApi")

    freeCompilerArgs.add("-Xcontext-sensitive-resolution")
}

testing.suites.named("test", JvmTestSuite::class) {
    dependencies {
        implementation(testFixtures(projects.proxyDsl))
    }
}
