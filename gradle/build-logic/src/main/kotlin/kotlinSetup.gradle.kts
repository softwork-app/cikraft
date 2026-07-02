plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("setup")
    id("jvm-test-suite")
    id("java-test-fixtures")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
}

publishing.publications.register<MavenPublication>("gpr") {
    from(components["java"])
}

java {
    withJavadocJar()
    withSourcesJar()
}

testing.suites {
    withType(JvmTestSuite::class).configureEach {
        useKotlinTest()
    }
}
