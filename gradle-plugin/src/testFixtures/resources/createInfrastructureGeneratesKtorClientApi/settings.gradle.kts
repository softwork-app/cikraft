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
                    name = "KDGP"
                    credentials(org.gradle.api.credentials.PasswordCredentials::class)
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
    repositories {
        google()
    }
    versionCatalogs.register("libs") {
        from(files("../../../../../gradle/libs.versions.toml"))
    }
}

rootProject.name = "createInfrastructureGeneratesKtorClientApi"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

includeBuild("../../../../../")

include(":app")
include(":infra")
include(":client")

defaults {
    jvmApplication {
        cikraft {
            infrastructure {
                httpNamespace = "foo"
                suffix = providers.gradleProperty("suffix")

                apiStages {
                    apiStage("Dev") {
                        apiServer = "foo"
                        authServer = "bar"
                        httpServer = "localhost"
                        web = "localhost"
                    }
                }
                transportStages {
                    transportStage("Prd") {
                        httpServer = "prodHost"
                        web = "localhost"
                    }
                }

                integrationArtifacts { }
            }
        }
    }
}
