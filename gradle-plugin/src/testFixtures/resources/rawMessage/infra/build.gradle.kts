jvmApplication {
    ciKraftInfrastructure {
        suffix = providers.gradleProperty("suffix")

        apiStages {
            apiStage("Dev") {
                apiServer = "foo"
                authServer = "bar"
                httpServer = "localhost"
                web = "localhost"
            }
        }

        httpNamespace = "foo"

        integrationArtifacts {
            integrationPackages {
                integrationPackage("IP_Foo") {
                    description = "Foo test"

                    integrationFlows {
                        integrationFlow("IF_Baz") {
                            description = "Baz test"

                            dependencies {
                                implementation(projects.app)
                            }
                            scripts.from("test.groovy")
                        }
                    }
                }
            }
        }
    }
}
