jvmApplication {
    cikraft {
        infrastructure {
            integrationArtifacts {
                dependencies {
                    implementation(integrationFlows(projects.app))
                }

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
