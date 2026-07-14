jvmApplication {
    toolchain.releaseVersion = 8

    kotlin {
        serialization {

        }
    }

    dependencies {
        implementation(libs.serialization.json)
    }

    iflow {

    }

    ciKraftInfrastructure {
        apiStages {
            apiStage("Sbx") {
                description = "Sandbox Stage for unit tests"
                web =
                    "https://b3d0decftrial.integrationsuite-trial.cfapps.us10-001.hana.ondemand.com/shell/home"
                apiServer = "https://8c5e4266trial.it-cpitrial03.cfapps.ap21.hana.ondemand.com"
                authServer = "https://8c5e4266trial.authentication.ap21.hana.ondemand.com"
                httpServer = "https://8c5e4266trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com/http"
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
