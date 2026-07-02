jvmApplication {
    ciKraftInfrastructure {
        apiStages {
            apiStage("Dev") {
                apiServer = "foo"
                authServer = "bar"
                httpServer = "https://localhost"
                web = "localhost"
            }
        }

        httpNamespace = "/foo"

        integrationArtifacts {
            integrationPackages {
                integrationPackage("Com_Example_Ktor_Resources") {
                    description = "A Description"

                    integrationFlows {
                        integrationFlow("IF_Ba") {
                            description = "Ba test"

                            dependencies {
                                implementation(projects.app)
                            }
                        }
                        integrationFlow("IF_Ba") {
                            description = "Ba test"

                            dependencies {
                                implementation(projects.app)
                            }
                        }
                        integrationFlow("IF_Foo") {
                            description = "Ba test"

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
