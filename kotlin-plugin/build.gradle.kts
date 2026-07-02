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

dependencies {
    annotationsRuntime(projects.runtime)
    annotationsRuntime(libs.serialization.json)
}

tasks.generateTests {
    mainClass.set("app.softwork.cikraft.kotlin.GenerateTestsKt")
}
