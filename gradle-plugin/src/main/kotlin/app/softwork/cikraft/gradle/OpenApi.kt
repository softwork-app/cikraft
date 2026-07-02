package app.softwork.cikraft.gradle

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition

interface OpenApiDefinition : Definition<BuildModel.None> {
    val title: Property<String>
    val description: Property<String>

    @get:Nested
    val dependencies: OpenApiDependencies
}

interface OpenApiDependencies : Dependencies {
    val infrastructure: DependencyCollector
    val transformers: DependencyCollector
}
