package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
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
import javax.inject.Inject

@BindsProjectFeature(GeneratePropertiesFeature::class)
public abstract class GeneratePropertiesFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("generateProperties", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
    }

    protected abstract class ApplyAction :
        ProjectFeatureApplyAction<GeneratePropertiesDefinition, BuildModel.None, SAPCIGeneratorDefinition> {
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
            definition: GeneratePropertiesDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIGeneratorDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            val buildModelName = parentBuildModel.name
            val stage = definition.stage.get()

            val propertiesConfiguration = configurations.resolvable(
                "cikraftInfrastructureProperties$stage$buildModelName",
            ) {
                it.fromDependencyCollector(definition.dependencies.infrastructure)
                it.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.STAGE_PROPERTIES))
                }
            }

            val functionsWorker = configurations.dependencyScope("cikraftPropertiesWorker$stage$buildModelName") {
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:flow-dsl:$VERSION"))
            }
            val functionsWorkerClasspath = configurations.resolvable(
                "cikraftPropertiesWorkerClasspath$stage$buildModelName",
            ) {
                it.extendsFrom(functionsWorker)
            }

            val sapCICreatedFlows = configurations.resolvable(
                "cikraftGeneratePropertiesCreatedFlow$stage$buildModelName",
            ) {
                it.fromDependencyCollector(definition.dependencies.infrastructure)
                it.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.API))
                }
            }

            val task = tasks.register("generateProperties$stage$buildModelName", GeneratePropertiesTask::class.java) {
                it.createdFlows.from(sapCICreatedFlows)
                it.stage.set(stage)
                it.propertiesFiles.from(propertiesConfiguration)
                it.propertiesFolder.convention(
                    layout.contextBuildDirectory.map {
                        val sourceSetName = buildModelName.ifEmpty { "main" }
                        it.dir(
                            "generated/cikraft/$sourceSetName/$stage/kotlin/properties",
                        )
                    },
                )
                it.workerClasspath.from(functionsWorkerClasspath)
            }

            parentBuildModel.sourceDirectorySet.srcDir(task)
        }
    }
}

public interface GeneratePropertiesDefinition : Definition<BuildModel.None> {
    @get:Nested
    public val dependencies: IFlowDependencies

    public val stage: Property<String>
}
