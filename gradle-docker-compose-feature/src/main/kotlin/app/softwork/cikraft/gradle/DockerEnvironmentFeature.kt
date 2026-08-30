package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.Definition
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.kotlin.dsl.named
import tel.schich.tinyjib.TinyJibExtension
import java.util.*
import javax.inject.Inject

@BindsProjectFeature(DockerEnvironmentFeature::class)
abstract class DockerEnvironmentFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("cikraft", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<DockerEnvironmentDefinition, BuildModel.None, JibDefinition> {
        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        @get:Inject
        abstract val project: Project

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: DockerEnvironmentDefinition,
            buildModel: BuildModel.None,
            parentDefinition: JibDefinition,
        ) {
            val jib = project.extensions.getByName("tinyJib") as TinyJibExtension

            val containerWorkerDeps = configurations.dependencyScope("containerWorkerDeps") {
                @Suppress("INVISIBLE_REFERENCE")
                dependencies.add(
                    dependencyFactory.create("app.softwork.cikraft:core:${app.softwork.cikraft.gradle.VERSION}"),
                )
            }

            val containerWorkerClasspath = configurations.resolvable("containerWorkerClasspath") {
                extendsFrom(containerWorkerDeps)
            }

            val propertiesConfiguration = configurations.resolvable("dockerComposeProperties") {
                fromDependencyCollector(definition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.STAGE_PROPERTIES))
                    attributeProvider(SAPCIStage.attribute, definition.stage.map { named(it) })
                }
            }

            val writePropertiesToFile =
                tasks.register("writePropertiesToFile", WritePropertiesToFile::class.java) {
                    propertyFiles.from(propertiesConfiguration)
                    output.convention(
                        layout.contextBuildDirectory.map { it.file("cikraft/docker/properties.properties") },
                    )
                    workerClasspath.from(containerWorkerClasspath)
                }
            jib.container.environment.putAll(
                writePropertiesToFile.flatMap { it.output }.flatMap {
                    project.provider {
                        val s = it.asFile.inputStream().use { Properties().apply { load(it) } }
                        @Suppress("UNCHECKED_CAST")
                        s as Map<String, String>
                    }
                },
            )
        }
    }
}

interface DockerEnvironmentDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: DockerEnvironmenDependencies

    val stage: Property<String>
}

interface DockerEnvironmenDependencies : Dependencies {
    val infrastructure: DependencyCollector
}
