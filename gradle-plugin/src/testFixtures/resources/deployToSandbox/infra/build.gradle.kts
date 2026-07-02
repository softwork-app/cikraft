jvmApplication {
    toolchain.releaseVersion = 8

    ciKraftInfrastructure {
        apiStages {
            apiStage("Sbx") {
                description = "Sandbox Stage for unit tests"
                web =
                    "https://b3d0decftrial.integrationsuite-trial.cfapps.us10-001.hana.ondemand.com/shell/home"
                apiServer = "https://b3d0decftrial.it-cpitrial05.cfapps.us10-001.hana.ondemand.com"
                authServer = "https://b3d0decftrial.authentication.us10.hana.ondemand.com"
                httpServer = "https://b3d0decftrial.it-cpitrial05-rt.cfapps.us10-001.hana.ondemand.com/http"
            }
        }

        httpNamespace = "/foo"

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
