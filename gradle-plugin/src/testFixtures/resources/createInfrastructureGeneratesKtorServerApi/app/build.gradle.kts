jvmApplication {
  toolchain.releaseVersion = 8

  kotlin {
    serialization {

    }
  }

  cikraft {

  }

  dependencies {
    implementation(projects.fault)
  }
}
