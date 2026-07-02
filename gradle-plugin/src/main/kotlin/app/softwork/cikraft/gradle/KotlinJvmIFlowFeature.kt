package app.softwork.cikraft.gradle

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
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
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(KotlinJvmIFlowFeature::class)
abstract class KotlinJvmIFlowFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("iflow", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
            .withBuildModelImplementationType(DefaultIFlowBuildModel::class.java)
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<IFlowDefinition, IFlowBuildModel, JvmApplicationProjectType> {
        @get:Inject
        abstract val pluginManager: PluginManager

        @get:Inject
        abstract val tasks: TaskContainer

        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val configurationsContainer: ConfigurationContainer

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject abstract val layout: ProjectFeatureLayout

        @get:Inject abstract val objects: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: IFlowDefinition,
            buildModel: IFlowBuildModel,
            parentDefinition: JvmApplicationProjectType,
        ) {
            buildModel as DefaultIFlowBuildModel

            buildModel.sapCIInfrastructureApi = configurations.resolvable("cikraftInfrastructureApi") {
                fromDependencyCollector(definition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.API))
                }
            }

            pluginManager.apply(SapCIKotlinPlugin::class.java) // Needed because KGP uses afterEvaluate to setup compiler plugins

            pluginManager.apply("com.google.devtools.ksp")
            configurationsContainer.getByName(
                "ksp",
            ).dependencies.add(dependencyFactory.create("app.softwork.cikraft:ksp-plugin:$VERSION"))

            configurations.consumable("cikraftEntryPoints") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.JSON_ENTRYPOINTS))
                }
                outgoing {
                    val lazyArtifact = objects.newInstance(
                        LazyDirectoryArtifact::class,
                        "json",
                        "entrypoints",
                        "kspKotlin",
                        layout.contextBuildDirectory.map {
                            it.file(
                                "generated/ksp/main/resources/cikraft/entrypoint.json",
                            )
                        },
                    )
                    artifact(lazyArtifact)
                }
            }

            parentDefinition.dependencies.implementation.add("app.softwork.cikraft:runtime:$VERSION")
            parentDefinition.dependencies.compileOnly.add(SAPCI_SCRIPT_API)
            parentDefinition.dependencies.compileOnly.add(SAPCI_GENERIC_API)

            tasks.named("processResources", ProcessResources::class) {
                exclude("cikraft/entrypoint.json")
            }
        }
    }
}

interface IFlowDefinition : Definition<IFlowBuildModel> {
    @get:Nested
    val dependencies: IFlowDependencies
}

interface IFlowBuildModel : BuildModel {
    val sapCIInfrastructureApi: NamedDomainObjectProvider<ResolvableConfiguration>
}

abstract class DefaultIFlowBuildModel : IFlowBuildModel {
    override lateinit var sapCIInfrastructureApi: NamedDomainObjectProvider<ResolvableConfiguration>
}
