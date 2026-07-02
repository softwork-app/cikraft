package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Property
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
import javax.inject.Inject

@BindsProjectFeature(GeneratePropertiesFeature::class)
abstract class GeneratePropertiesFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("generateProperties", ApplyAction::class)
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<GeneratePropertiesDefinition, BuildModel.None, SAPCIGeneratorDefinition> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: GeneratePropertiesDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIGeneratorDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            val stage = definition.stage.get()

            val propertiesConfiguration = configurations.resolvable("cikraftInfrastructureProperties" + stage) {
                fromDependencyCollector(parentDefinition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.STAGE_PROPERTIES))
                    attribute(SAPCIStage.attribute, named(stage))
                }
            }

            val functionsWorker = configurations.dependencyScope("cikraftPropertiesWorker" + stage) {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:flow-dsl:$VERSION"))
            }
            val functionsWorkerClasspath = configurations.resolvable("cikraftPropertiesWorkerClasspath" + stage) {
                extendsFrom(functionsWorker)
            }

            val task = tasks.register("generateProperties" + stage, GeneratePropertiesTask::class.java) {
                createdFlows.set(parentBuildModel.sapCICreatedFlows)
                propertiesFiles.fileProvider(propertiesConfiguration.flatMap { it.elements.map { it.single().asFile } })
                propertiesFolder.convention(layout.contextBuildDirectory.map { it.dir("cikraft/properties$stage") })
                workerClasspath.from(functionsWorkerClasspath)
            }

            parentBuildModel.sourceDirectorySet.srcDir(task)
        }
    }
}

interface GeneratePropertiesDefinition : Definition<BuildModel.None> {
    val stage: Property<String>
}
