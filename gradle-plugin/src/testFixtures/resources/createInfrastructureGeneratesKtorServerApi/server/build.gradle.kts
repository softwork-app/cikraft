jvmApplication {
  dependencies {
    implementation(projects.app)
    implementation(projects.fault)
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
      api(cikraftLibs.sapci.generic.api)
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
