jvmApplication {
  dependencies {
    implementation(projects.app)
    implementation(projects.fault)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.resources)
    implementation(cikraftLibs.ktorServerRuntime)
  }

  cikraft {
    generateKtorResources {
      dependencies {
        infrastructure(projects.infra)
      }
    }
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
    generateFunctions {
      dependencies {
        infrastructure(projects.infra)
      }
    }
  }
}
