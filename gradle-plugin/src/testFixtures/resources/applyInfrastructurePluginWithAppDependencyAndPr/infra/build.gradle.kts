jvmApplication {
    ciKraftInfrastructure {
        apiStages {
            apiStage("Dev") {
                apiServer = "foo"
                authServer = "bar"
            }
        }

        httpNamespace = "foo"

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
