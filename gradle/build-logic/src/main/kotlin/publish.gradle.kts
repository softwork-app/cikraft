plugins {
    id("maven-publish")
    id("signing")
}

publishing {
    repositories {
        maven(url = "https://maven.pkg.github.com/softwork-app/cikraft") {
            name = "GitHubPackages"
            credentials(PasswordCredentials::class)
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "Softwork.app - cikraft"
            description = "Automatically deploy your typesafe generated Integration Flows at the SAP Integration Suite"
            url = "https://github.com/softwork-app/cikraft"
            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            developers {
                developer {
                    id = "hfhbd"
                    name = "Philip Wedemann"
                    email = "mybztg+mavencentral@icloud.com"
                }
            }
            scm {
                connection = "scm:git://github.com/softwork-app/cikraft.git"
                developerConnection = "scm:git://github.com/softwork-app/cikraft.git"
                url = "https://github.com/softwork-app/cikraft"
            }

            distributionManagement {
                repository {
                    id = "github"
                    name = "GitHub hfhbd Apache Maven Packages"
                    url = "https://maven.pkg.github.com/softwork-app/cikraft"
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull,
        providers.gradleProperty("signingPassword").orNull,
    )
    isRequired = providers.gradleProperty("signingKey").isPresent
    sign(publishing.publications)
}
