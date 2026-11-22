package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage

public class OpenApiPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.dependencies.attributesSchema {
            it.attribute(SAPCI.attribute)
        }

        val sapCIInfrastructure = target.configurations.dependencyScope("cikraftOpenAPI")

        target.configurations.resolvable("cikraftOpenAPIFiles") {
            it.extendsFrom(sapCIInfrastructure)
            it.attributes {
                it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.OPENAPI))
            }
        }
    }
}
