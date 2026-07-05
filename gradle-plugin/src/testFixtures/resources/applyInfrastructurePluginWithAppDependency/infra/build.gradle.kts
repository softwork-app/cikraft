jvmApplication {
    ciKraftInfrastructure {
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
                                implementation(projects.app)
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

tasks.withType<io.github.hfhbd.r8.R8JarTask>().configureEach {
    // We don't use slf4j
    additionalRules.add("-dontwarn org.slf4j.*")
}
