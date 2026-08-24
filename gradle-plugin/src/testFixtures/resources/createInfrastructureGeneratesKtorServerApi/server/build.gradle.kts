jvmApplication {
  dependencies {
    implementation(projects.app)
    implementation(projects.fault)
    implementation(cikraftLibs.ktor.server.runtime)
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
      api(cikraftLibs.generic.api)
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
          implementation(cikraftLibs.ktor.server.runtime)
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
