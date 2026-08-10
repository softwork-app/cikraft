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
                    integrationPackage("Com_Example_Ktor_Resources") {
                        integrationFlows {
                            integrationFlow("IF_Ba") {
                                description = "Ba test"

                                dependencies {
                                    implementation(project())
                                }
                            }
                            integrationFlow("IF_Foo") {
                                description = "Ba test"

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
