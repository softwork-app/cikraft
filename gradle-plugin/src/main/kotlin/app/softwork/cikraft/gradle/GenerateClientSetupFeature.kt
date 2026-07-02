package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyFactory
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
import javax.inject.Inject

@BindsProjectFeature(GenerateClientSetupFeature::class)
abstract class GenerateClientSetupFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("generateKtorClientSetup", ApplyAction::class)
            .withUnsafeApplyAction()
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<GenerateClientSetupDefinition, BuildModel.None, SAPCIGeneratorDefinition> {
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
            definition: GenerateClientSetupDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIGeneratorDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            val buildModelName = parentBuildModel.name

            val sapciKtorClientSetupWorker = configurations.dependencyScope(
                "cikraftKtorClientSetupWorker$buildModelName",
            ) {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }

            val functionsWorkerClasspath = configurations.resolvable(
                "cikraftKtorClientSetupWorkerClasspath$buildModelName",
            ) {
                extendsFrom(sapciKtorClientSetupWorker)
            }

            val task = tasks.register("generateKtorClientSetup$buildModelName", GenerateKtorClientSetup::class.java) {
                createdFlows.set(parentBuildModel.sapCICreatedFlows)
                ktorApi.convention(
                    layout.contextBuildDirectory.map { it.dir("cikraft/ktor/client/setup$buildModelName") },
                )
                workerClasspath.from(functionsWorkerClasspath)
            }
            parentBuildModel.sourceDirectorySet.srcDir(task)
        }
    }
}

interface GenerateClientSetupDefinition : Definition<BuildModel.None>
