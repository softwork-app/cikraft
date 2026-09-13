plugins {
    `kotlin-dsl`
    id("setup")
    id("java-test-fixtures")
    id("jvm-test-suite")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.kotlin.ecosystem)
    implementation(libs.jib.feature)
    implementation(projects.gradlePlugin)

    compileOnly(projects.core)
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

gradlePlugin {
    plugins.configureEach {
        displayName = "Gradle plugin to configure ksp for SAPCI entrypoint generation"
        description = "Gradle plugin to configure ksp for SAPCI entrypoint generation"
    }

    plugins.register("app.softwork.cikraft.jib-feature") {
        implementationClass = "app.softwork.cikraft.gradle.JibFeaturePlugin"
    }
}

configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradleVersion.version("9.7").version)
        )
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
