jvmApplication {
    toolchain.releaseVersion = 8

    kotlin {
        serialization {

        }
    }

    iflow {

    }

    api {
        dependencies {
            api(libs.serialization.json)
        }
    }
}
