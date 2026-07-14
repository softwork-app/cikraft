jvmApplication {
    ciKraftInfrastructure {
        apiStages {
            apiStage("Dev") {
                apiServer = "foo"
                authServer = "bar"
                httpServer = "localhost"
                web = "localhost"
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
