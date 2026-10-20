pluginManagement {
    includeBuild("../../../../../gradle/build-logic")
    includeBuild("../../../../../")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        exclusiveContent {
            forRepository {
                maven {
                    setUrl(
                        "https://maven.pkg.github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin"
                    )
                    credentials(PasswordCredentials::class) {
                        username = providers.gradleProperty("GitHubPackagesUsername").get()
                        password = providers.gradleProperty("GitHubPackagesPassword").get()
                    }
                }
            }
            filter {
                includeGroupAndSubgroups("org.jetbrains.ecosystem")
            }
        }
    }
}

plugins {
    id("myRepos")
    id("org.jetbrains.ecosystem").version("0.97.0")
    id("app.softwork.cikraft.ecosystem")
}

dependencyResolutionManagement {
    versionCatalogs.register("libs") {
        from(files("../../../../../gradle/libs.versions.toml"))
    }
}

rootProject.name = "applyInfrastructurePluginWithAppDependencyAndPr"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

includeBuild("../../../../../")

include(":app")
