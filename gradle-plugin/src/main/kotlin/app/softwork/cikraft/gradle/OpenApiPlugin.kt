package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.named

class OpenApiPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.dependencies.attributesSchema {
            attribute(SAPCI.attribute)
        }

        val sapCIInfrastructure = target.configurations.dependencyScope("ciKraftInfrastructure")

        target.configurations.resolvable("ciKraftOpenAPI") {
            extendsFrom(sapCIInfrastructure.get())
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                attribute(SAPCI.attribute, named(SAPCI.OPENAPI))
            }
        }
    }
}
