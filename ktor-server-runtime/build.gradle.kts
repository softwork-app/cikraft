plugins {
    id("kotlinMPPSetup")
    kotlin("plugin.serialization")
}

kotlin.sourceSets {
    commonMain {
        dependencies {
            api(projects.runtime)
            api(libs.serialization.core)
            api(libs.ktor.server.core)
        }
    }
    commonTest {
        dependencies {
            implementation(libs.serialization.json)
            implementation(libs.serialization.xml)
            implementation(libs.ktor.server.test.host)
        }
    }
}
