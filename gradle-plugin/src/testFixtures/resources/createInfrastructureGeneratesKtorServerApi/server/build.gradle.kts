jvmApplication {
  dependencies {
    implementation(projects.app)
    implementation(projects.fault)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.resources)
    implementation(ciKraftLibs.ktorServerRuntime)
  }

  ciKraft {
    dependencies {
      infrastructure(projects.infra)
    }

    generateKtorResources {}
    generateKtorServerApi {}
    generateProperties {
      stage = "Dev"
    }
    generateFunctions {}
  }
}
