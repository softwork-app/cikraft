jvmApplication {
    toolchain.releaseVersion = 8

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
                integrationPackage("IP_Multiple") {
                    description = "Foo test"

                    integrationFlows {
                        integrationFlow("IF_Multiple") {
                            description = "Multiple test"

                            dependencies {
                                implementation(projects.app)
                                implementation(projects.core)
                            }
                        }
                    }
                }
            }
        }
    }
}
