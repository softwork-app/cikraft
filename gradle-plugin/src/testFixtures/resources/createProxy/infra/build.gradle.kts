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
            suffix = "/pr"

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
                this.apiProxies.addLater(suffix.orElse("").map {
                    objects.newInstance(app.softwork.cikraft.gradle.ApiProxy::class, "Test_API_FOO_URL${it.replace("/", "_")}").apply {
                        virtualHostName = "default"
                    }
                })

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
