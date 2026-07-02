plugins {
    id("publish")
    id("org.jetbrains.dokka")
    id("dev.detekt")
}

dokka {
    val module = project.name
    dokkaSourceSets.configureEach {
        includes.from("README.md")
        val sourceSetName = name
        File("$module/src/$sourceSetName").takeIf { it.exists() }?.let {
            sourceLink {
                localDirectory.set(file("src/$sourceSetName/kotlin"))
                remoteUrl.set(uri("https://github.com/softwork-app/cikraft/tree/main/$module/src/$sourceSetName/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
        externalDocumentationLinks {
            register("kotlinx.coroutines") {
                url("https://kotlinlang.org/api/kotlinx.coroutines/")
            }
            register("kotlinx.serialization") {
                url("https://kotlinlang.org/api/kotlinx.serialization/")
            }
            register("ktor") {
                url("https://api.ktor.io/")
            }
        }
    }
}

detekt {
    parallel = true
    autoCorrect = true
    buildUponDefaultConfig = true
    ignoreFailures = providers.gradleProperty("ignoreDetektFailures").map { it.toBoolean() }.orElse(false)

    dependencies {
        detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:${detekt.toolVersion.get()}")
    }
}

tasks.register<Delete>("deleteDetektBaseline") {
    delete(tasks.detekt.flatMap { it.baseline })
}

configurations.consumable("sarif") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("detekt-sarif"))
    }
    outgoing {
        artifact(tasks.detekt.flatMap { it.reports.sarif.outputLocation })
    }
}
