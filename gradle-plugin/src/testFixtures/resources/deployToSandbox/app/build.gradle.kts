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
                    web =
                        "https://5f44b7f9trial.integrationsuite-trial.cfapps.ap21.hana.ondemand.com/shell/home"
                    apiServer = "https://5f44b7f9trial.it-cpitrial03.cfapps.ap21.hana.ondemand.com"
                    authServer = "https://5f44b7f9trial.authentication.ap21.hana.ondemand.com"
                    httpServer = "https://5f44b7f9trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com/http"
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
                                    implementation(projects.app)
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
