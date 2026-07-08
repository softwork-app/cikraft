plugins {
    id("kotlinSetup")
    id("app.softwork.serviceloader-compiler")
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
        annotation(libs.serialization.json)
    }
}
