package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface IntegrationFlow :
    Definition<IntegrationFlowBuildModel>,
    Named {
    @get:Nested
    val dependencies: IntegrationFlowDependencies

    val description: Property<String>

    val source: ListProperty<String>
    val target: ListProperty<String>

    val scripts: ConfigurableFileCollection
}

interface IntegrationFlowDependencies : Dependencies {
    val implementation: DependencyCollector
}

interface IntegrationFlowBuildModel :
    BuildModel,
    Named {
    val description: Property<String>
    val source: ListProperty<String>
    val target: ListProperty<String>

    val flowID: Provider<String>

    val libs: ConfigurableFileCollection
    val scripts: ConfigurableFileCollection
    val generatedScripts: ConfigurableFileCollection

    val sapciRuntimeLibs: DependencyCollector

    val kotlinEntrypointImplementation: DependencyCollector
    val kotlinEntryPointsClasses: ConfigurableFileTree
}

abstract class DefaultIntegrationFlowBuildModel : IntegrationFlowBuildModel {
    override fun getName(): String = internalName
    lateinit var internalName: String
    abstract override val flowID: Property<String>

    abstract override var kotlinEntryPointsClasses: ConfigurableFileTree
}
