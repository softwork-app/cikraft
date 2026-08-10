package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
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
import javax.inject.Inject

@BindsProjectFeature(GenerateFunctionsFeature::class)
abstract class GenerateFunctionsFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("generateFunctions", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<GenerateFunctionsDefinition, BuildModel.None, SAPCIGeneratorDefinition> {
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
            definition: GenerateFunctionsDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIGeneratorDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            val buildModelName = parentBuildModel.name

            val workerDeps = configurations.dependencyScope("cikraftFunctionsWorker$buildModelName") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val functionsWorkerClasspath = configurations.resolvable(
                "cikraftFunctionsWorkerClasspath$buildModelName",
            ) {
                extendsFrom(workerDeps)
            }

            val sapCICreatedFlows = configurations.resolvable("cikraftFunctionsCreatedFlow$buildModelName") {
                fromDependencyCollector(definition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.API))
                }
            }.flatMap { it.elements }.map { it.single().asFile }

            val task = tasks.register("generateFunctions$buildModelName", GenerateFunctionsTask::class.java) {
                createdFlows.fileProvider(sapCICreatedFlows)
                functionsFolder.convention(
                    layout.contextBuildDirectory.map {
                        it.dir(
                            "generated/cikraft/${parentBuildModel.sourceDirectorySet.name}/functions",
                        )
                    },
                )
                workerClasspath.from(functionsWorkerClasspath)
            }
            parentBuildModel.sourceDirectorySet.srcDir(task)
        }
    }
}

interface GenerateFunctionsDefinition : Definition<BuildModel.None> {
    @get:Nested
    val dependencies: IFlowDependencies
}
