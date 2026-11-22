package app.softwork.cikraft.gradle

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

public interface OpenApiDefinition : Definition<BuildModel.None> {
    public val title: Property<String>
    public val description: Property<String>

    @get:Nested
    public val dependencies: OpenApiDependencies
}

public interface OpenApiDependencies : Dependencies {
    public val infrastructure: DependencyCollector
    public val transformers: DependencyCollector
}
