jvmApplication {
  toolchain.releaseVersion = 8

  kotlin {
    serialization {

    }
  }

  iflow {

  }

  dependencies {
    implementation(projects.fault)
  }
}
