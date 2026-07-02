pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("myRepos")
}

rootProject.name = "cikraft"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

include(":api")
include(":flow-dsl")
include(":proxy")
include(":proxy-dsl")

include(":gradle-plugin")
include(":ksp-plugin")
include(":kotlin-plugin")

include(":gradle-worker")

include(":core")
include(":generator")
include(":integration-flow-builder-runtime")

include(":runtime")

include(":ktor-server-runtime")
include(":ktor-server-engine")
