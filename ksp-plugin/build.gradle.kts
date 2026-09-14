plugins {
    id("kotlinSetup")
    id("io.github.hfhbd.serviceloader")
}

kotlin.jvmToolchain(21)

dependencies {
    implementation(libs.ksp.api)
    implementation(projects.generator)
}
