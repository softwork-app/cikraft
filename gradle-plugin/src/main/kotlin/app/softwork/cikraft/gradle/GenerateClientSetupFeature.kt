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
import javax.inject.Inject

@BindsProjectFeature(GenerateClientSetupFeature::class)
public abstract class GenerateClientSetupFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("generateKtorClientSetup", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
    }

    public abstract class ApplyAction :
        ProjectFeatureApplyAction<GenerateClientSetupDefinition, BuildModel.None, SAPCIGeneratorDefinition> {
        @get:Inject
        protected abstract val configurations: ConfigurationRegistrar

        @get:Inject
        protected abstract val tasks: TaskRegistrar

        @get:Inject
        protected abstract val dependencyFactory: DependencyFactory

        @get:Inject
        protected abstract val layout: ProjectFeatureLayout

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: GenerateClientSetupDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIGeneratorDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            val buildModelName = parentBuildModel.name

            val workerDeps = configurations.dependencyScope(
                "cikraftKtorClientSetupWorker$buildModelName",
            ) {
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val functionsWorkerClasspath = configurations.resolvable(
                "cikraftKtorClientSetupWorkerClasspath$buildModelName",
            ) {
                it.extendsFrom(workerDeps)
            }

            val sapCICreatedFlows = configurations.resolvable("cikraftClientSetupCreatedFlow$buildModelName") {
                it.fromDependencyCollector(definition.dependencies.infrastructure)
                it.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.API))
                }
            }

            val task = tasks.register("generateKtorClientSetup$buildModelName", GenerateKtorClientSetup::class.java) {
                it.createdFlows.from(sapCICreatedFlows)
                it.ktorApi.convention(
                    layout.contextBuildDirectory.map {
                        val sourceSetName = parentBuildModel.name.ifEmpty { "main" }
                        it.dir(
                            "generated/cikraft/$sourceSetName/kotlin/ktor/client/setup",
                        )
                    },
                )
                it.workerClasspath.from(functionsWorkerClasspath)
            }
            parentBuildModel.sourceDirectorySet.srcDir(task)
        }
    }
}

public interface GenerateClientSetupDefinition : Definition<BuildModel.None> {
    @get:Nested
    public val dependencies: IFlowDependencies
}
