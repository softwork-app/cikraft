jvmApplication {
    toolchain.releaseVersion = 8

    kotlin {
        serialization {

        }
    }

    dependencies {
        implementation(projects.core)
    }

    cikraft {
        infrastructure {
            suffix = providers.gradleProperty("suffix")

            integrationArtifacts {
                integrationPackages {
                    integrationPackage("Bar") {
                        description = "Bar Description"
                        integrationFlows {
                            integrationFlow("IF_Bar") {
                                description = "Bar test"

                                dependencies {
                                    implementation(project())
                                }
                            }
                        }
                    }
                }

                openApi {
                    dependencies {
                        infrastructure(project())
                    }
                }
            }
        }
    }
}
