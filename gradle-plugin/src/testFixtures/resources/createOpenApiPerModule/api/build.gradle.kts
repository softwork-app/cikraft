jvmApplication {
    toolchain.releaseVersion = 8

    cikraft {
        infrastructure {
            suffix = providers.gradleProperty("suffix")

            integrationArtifacts {
                openApi {
                    dependencies {
                        infrastructure(projects.app)
                        infrastructure(projects.bar)
                    }
                }
            }
        }
    }
}
