jvmApplication {
    cikraft {
        infrastructure {
            apiStages {
                apiStage("Dev") {
                    apiServer = "foo"
                    authServer = "bar"
                    httpServer = "localhost"
                    web = "localhost"

                    apiVirtualHosts {
                        apiVirtualHost("default") {
                            id = "foo"
                            apiHttpServer = "https://api.example.com/http"
                            clientAuthEnabled = false
                        }
                    }
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
            apiProxies {
                apiRuntimeProviders {
                    apiRuntimeProvider("PRO_FOO") {
                        title = "My runtime provider"
                    }
                }
                this.apiProxies {
                    apiProxy("Test_API_FOO_URL") {
                        virtualHostName = "default"
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
}
