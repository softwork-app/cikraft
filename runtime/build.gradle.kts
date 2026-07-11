import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping

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

    publishing {
        adhocSoftwareComponent {
            addVariantsFromConfiguration(
                configurations.testFixturesApiElements.get(),
                JavaConfigurationVariantMapping("compile", true, configurations.testFixturesCompileClasspath.get()),
            )
            addVariantsFromConfiguration(
                configurations.testFixturesRuntimeElements.get(),
                JavaConfigurationVariantMapping("runtime", true, configurations.testFixturesRuntimeClasspath.get()),
            )
        }
    }
}

dependencies {
    testFixturesApi(libs.sapci.script.api)
    testFixturesApi(libs.sapci.generic.api)
    testFixturesApi(libs.apache.camel)
    testFixturesApi(libs.sapci.javax.activation)
}
