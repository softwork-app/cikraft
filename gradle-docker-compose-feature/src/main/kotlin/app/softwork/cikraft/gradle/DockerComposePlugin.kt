package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage

abstract class DockerComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.dependencies.attributesSchema {
            attribute(SAPCI.attribute)
        }

        val cikraft = target.configurations.dependencyScope("cikraftDockerCompose")

        target.configurations.resolvable("cikraftDockerComposeFiles") {
            extendsFrom(cikraft)
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, named(Usage::class.java, SAPCI_USAGE))
                attribute(SAPCI.attribute, named(SAPCI::class.java, SAPCI.DOCKER_COMPOSE))
            }
        }
    }
}
