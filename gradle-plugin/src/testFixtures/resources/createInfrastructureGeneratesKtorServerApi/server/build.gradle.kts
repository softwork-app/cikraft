import app.softwork.cikraft.gradle.SAPCI
import app.softwork.cikraft.gradle.SAPCIStage
import org.gradle.api.attributes.Usage

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

listOf(
    "Unstaged" to null,
    "Dev" to "Dev",
    "Prod" to "Prod",
).forEach { (name, stage) ->
    val configurationName = "${name.lowercase()}Properties"
    val configuration = configurations.create(configurationName) {
        isCanBeConsumed = false
        isCanBeResolved = true
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "sap-ci"))
            attribute(SAPCI.attribute, objects.named(SAPCI::class.java, SAPCI.STAGE_PROPERTIES))
            stage?.let {
                attribute(SAPCIStage.attribute, objects.named(SAPCIStage::class.java, it))
            }
        }
    }
    dependencies.add(configurationName, project(":infra"))

    tasks.register("resolve${name}Properties") {
        inputs.files(configuration)
        doLast {
            println("resolved=${inputs.files.singleFile}")
        }
    }
}
