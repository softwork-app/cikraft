jvmApplication {
    cikraft {
        infrastructure {
            apiStages {
                apiStage("Dev") {
                    apiServer = "foo"
                    authServer = "bar"
                    httpServer = "localhost"
                    web = "localhost"
                }
                apiStage("Prod") {
                    apiServer = "prod-foo"
                    authServer = "prod-bar"
                    httpServer = "prod-localhost"
                    web = "prod-localhost"
                }
            }

            httpNamespace = "foo"
            suffix = providers.gradleProperty("suffix")

            integrationArtifacts {
                integrationPackages {
                    integrationPackage("IP_Foo") {
                        description = "Foo test"

                        integrationFlows {
                            integrationFlow("IF_Ba") {
                                description = "Ba test"

                                dependencies {
                                    implementation(projects.app)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
