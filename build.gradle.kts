plugins {
    id("org.jetbrains.dokka")
    id("merge-detekt")
}

dokka {
    dokkaPublications.configureEach {
        includes.from("README.md")
    }

    dependencies {
        for (sub in subprojects) {
            dokka(sub)
        }
    }
}

mergeDetekt {
    dependencies {
        for (subproject in subprojects) {
            sarif(project(subproject.path))
        }
    }
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().downloadBaseUrl = null
}
