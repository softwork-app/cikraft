jvmApplication {
    toolchain.releaseVersion = 8

    kotlin {
        serialization {

        }
    }

    iflow {

    }

    dependencies {
        implementation(libs.serialization.json)
    }

    ciKraftInfrastructure {
        suffix = providers.gradleProperty("suffix")

        apiStages {
            apiStage("Dev") {
                apiServer = providers.gradleProperty("sapCIPort").map { "http://localhost:$it" }
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
                                implementation(project())
                            }

                            r8 {
                                additionalRules.add("-dontwarn org.slf4j.*")
                            }
                        }
                    }
                }
            }
            openApi {
                title = "Foo"
                dependencies {
                    infrastructure(project())
                }
            }
        }
    }
}
