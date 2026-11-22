package app.softwork.cikraft.gradle

import org.gradle.api.Named
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface IntegrationFlow :
    Definition<IntegrationFlowBuildModel>,
    Named {
    @get:Nested
    public val dependencies: IntegrationFlowDependencies

    public val description: Property<String>

    public val source: ListProperty<String>
    public val target: ListProperty<String>

    public val scripts: ConfigurableFileCollection

    public val autoNumbering: Property<Boolean>
}

public interface IntegrationFlowDependencies : Dependencies {
    public val implementation: DependencyCollector
}

public interface IntegrationFlowBuildModel :
    BuildModel,
    Named {
    public val description: Property<String>
    public val source: ListProperty<String>
    public val target: ListProperty<String>

    public val packageName: String

    public val flowID: Provider<String>

    public val libs: ConfigurableFileCollection
    public val scripts: ConfigurableFileCollection
    public val generatedScripts: ConfigurableFileCollection

    public val dependenciesJars: FileCollection
    public val kotlinEntryPointsClasses: FileCollection
    public val autoNumbering: Property<Boolean>
}

internal abstract class DefaultIntegrationFlowBuildModel : IntegrationFlowBuildModel {
    override fun getName(): String = internalName
    lateinit var internalName: String
    override lateinit var packageName: String

    abstract override val flowID: Property<String>

    abstract override val dependenciesJars: ConfigurableFileCollection
    abstract override val kotlinEntryPointsClasses: ConfigurableFileCollection
}
