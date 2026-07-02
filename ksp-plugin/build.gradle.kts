plugins {
    id("kotlinSetup")
    id("app.softwork.serviceloader-compiler")
}

kotlin.jvmToolchain(21)

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.generator)
}
