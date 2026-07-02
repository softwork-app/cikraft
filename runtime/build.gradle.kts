plugins {
    id("kotlinMPPSetup")
    id("java-test-fixtures")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.serialization.xml)
            }
        }
    }
}

dependencies {
    testFixturesApi(libs.sapci.script.api)
    testFixturesApi(libs.sapci.generic.api)
    testFixturesApi(libs.apache.camel)
    testFixturesApi(libs.sapci.javax.activation)
}
