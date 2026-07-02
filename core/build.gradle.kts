plugins {
    id("kotlinSetup")
}

dependencies {
    api(libs.serialization.core)
    api(libs.kfx.openapi.model)
    api(libs.kfx.core)

    testImplementation(libs.validation.runtime)
}
