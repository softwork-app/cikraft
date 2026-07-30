package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface SAPCIIFlowsDefinition : Definition<SAPCIIFlowsBuildModel> {
    val integrationPackages: NamedDomainObjectContainer<IntegrationPackage>

    @get:Nested val dependencies: JvmComponentDependencies
}

interface SAPCIIFlowsBuildModel : BuildModel {
    val integrationPackages: NamedDomainObjectContainer<IntegrationPackage>
    val suffix: Provider<String>
    val httpSuffix: Provider<String>
}

abstract class DefaultSAPCIIFlowsBuildModel : SAPCIIFlowsBuildModel {
    abstract override val httpSuffix: Property<String>
    abstract override val suffix: Property<String>

    // Workaround:
    // only used for the OpenApi Feature until a feature can access the parents parentBuildModel
    abstract val openApiStages: NamedDomainObjectContainer<Stage>
}
