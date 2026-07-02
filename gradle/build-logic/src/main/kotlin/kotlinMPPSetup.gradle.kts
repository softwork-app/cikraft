import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("setup")
}

kotlin {
    jvmToolchain(8)
    explicitApi()
    compilerOptions {
        allWarningsAsErrors.set(true)
    }

    jvm()

    js {
        nodejs()
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

plugins.withType<NodeJsPlugin> {
    the<NodeJsEnvSpec>().downloadBaseUrl = null
}
