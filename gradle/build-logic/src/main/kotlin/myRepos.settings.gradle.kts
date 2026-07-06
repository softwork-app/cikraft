plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        mavenCentral()

        google()

        exclusiveContent {
            forRepository {
                maven {
                    setUrl(
                        "https://maven.pkg.github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin"
                    )
                    name = "KDGP"
                    metadataSources {
                        gradleMetadata()
                    }
                    credentials(org.gradle.api.credentials.PasswordCredentials::class)
                }
            }
            filter {
                includeGroupAndSubgroups("org.jetbrains.ecosystem")
            }
        }

        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("org.nodejs", "node") }
                }
            }
            filter { includeGroup("org.nodejs") }
        }
    }
}
