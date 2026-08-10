jvmApplication {
  dependencies {
    implementation(projects.app)
    implementation(projects.fault)
    implementation(cikraftLibs.ktorServerRuntime)
  }

  cikraft {
    generateFunctions {
      dependencies {
        infrastructure(projects.infra)
      }
    }
  }

  testFixtures {
    dependencies {
      api(projects.app)
      api(projects.fault)
      api(libs.ktor.server.resources)
    }

    cikraft {
      generateKtorResources {
        dependencies {
          infrastructure(projects.infra)
        }
      }
    }
  }

  testSuites {
    suites {
      jvmTestSuite("test") {
        dependencies {
          implementation(cikraftLibs.ktorServerRuntime)
        }
        cikraft {
          generateKtorServerApi {
            dependencies {
              infrastructure(projects.infra)
            }
          }
          generateProperties {
            stage = "Dev"
            dependencies {
              infrastructure(projects.infra)
            }
          }
        }
      }
    }
  }
}
