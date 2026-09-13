jvmApplication {
    toolchain.releaseVersion = 8

    kotlin {
        serialization {

        }
    }

    dependencies {
        implementation(libs.serialization.json)
    }

    cikraft {
        infrastructure {
            apiStages {
                apiStage("Sbx") {
                    description = "Sandbox Stage for unit tests"
                    web = providers.gradleProperty("trialWeb")
                    apiServer = providers.gradleProperty("trialApiServer")
                    authServer = providers.gradleProperty("trialAuthServer")
                    httpServer = providers.gradleProperty("trialHttpServer").map { "$it/http" }
                }
            }

            httpNamespace = "/foo"
            suffix = providers.gradleProperty("suffix")

            integrationArtifacts {
                integrationPackages {
                    integrationPackage("IP_0100_Test_PW") {
                        description = "API Test - Automatic PR"

                        integrationFlows {
                            integrationFlow("IF_0100_Test_PW_SBX") {
                                description = "API Test - Automatic PR"

                                dependencies {
                                    implementation(projects.app)
                                }
                            }
                            integrationFlow("IF_0100_Test_PW_SBX_Exception") {
                                description = "API Test - Automatic PR"

                                dependencies {
                                    implementation(project())
                                }
                                r8 {

                                }
                            }
                        }
                    }
                }

                openApi {
                    title = "New IP"
                    description = "IP Description"
                    dependencies {
                        infrastructure(project())
                    }
                }
            }
        }
    }
}
