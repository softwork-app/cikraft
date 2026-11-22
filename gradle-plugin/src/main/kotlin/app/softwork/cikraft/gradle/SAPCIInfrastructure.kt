package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyModifier
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface SAPCIIFlowsDefinition : Definition<SAPCIIFlowsBuildModel> {
    public val integrationPackages: NamedDomainObjectContainer<IntegrationPackage>

    @get:Nested
    public val dependencies: SAPCIIFlowsDependencies
}

public interface SAPCIIFlowsDependencies : JvmComponentDependencies {
    @get:Nested
    public val integrationFlows: IntegrationFlowsDependencyModifier

    public abstract class IntegrationFlowsDependencyModifier : DependencyModifier() {
        override fun modifyImplementation(dependency: ModuleDependency) {
            dependency.capabilities {
                it.requireFeature("integration-flows")
            }
        }
    }
}

public interface SAPCIIFlowsBuildModel : BuildModel {
    public val integrationPackages: NamedDomainObjectContainer<IntegrationPackage>
    public val suffix: Provider<String>
    public val httpSuffix: Provider<String>
    public val stages: NamedDomainObjectContainer<Stage>
}

internal abstract class DefaultSAPCIIFlowsBuildModel : SAPCIIFlowsBuildModel {
    abstract override val httpSuffix: Property<String>
    abstract override val suffix: Property<String>
}
