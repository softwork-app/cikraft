plugins {
    id("kotlinSetup")
    id("io.github.hfhbd.serviceloader")
    id("io.github.hfhbd.kotlin-compiler-testing")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}

kotlinTesting {
    mainClass = "app.softwork.cikraft.kotlin.GenerateTestsKt"

    dependencies {
        annotation(projects.runtime)
        annotation(libs.sapci.generic.api)
        annotation(libs.serialization.json)
    }
}
