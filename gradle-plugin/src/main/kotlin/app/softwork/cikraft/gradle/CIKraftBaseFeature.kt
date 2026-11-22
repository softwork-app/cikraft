package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.TaskContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(CIKraftBaseFeature::class)
public abstract class CIKraftBaseFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("cikraft", ApplyAction::class)
            .withUnsafeApplyAction()
            .withBuildModelImplementationType(DefaultSAPCIGeneratorBuildModel::class.java)
    }

    public abstract class ApplyAction :
        ProjectFeatureApplyAction<CiKraftBaseDefinition, SAPCIGeneratorBuildModel, JvmApplicationProjectType> {
        @get:Inject
        protected abstract val pluginManager: PluginManager

        @get:Inject
        protected abstract val tasks: TaskContainer

        @get:Inject
        protected abstract val configurations: ConfigurationRegistrar

        @get:Inject
        protected abstract val configurationsContainer: ConfigurationContainer

        @get:Inject
        protected abstract val dependencyFactory: DependencyFactory

        @get:Inject protected abstract val layout: ProjectFeatureLayout

        @get:Inject protected abstract val objects: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: CiKraftBaseDefinition,
            buildModel: SAPCIGeneratorBuildModel,
            parentDefinition: JvmApplicationProjectType,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            pluginManager.apply(SapCIKotlinPlugin::class.java) // Needed because KGP uses afterEvaluate to setup compiler plugins

            pluginManager.apply("com.google.devtools.ksp")
            configurationsContainer.getByName(
                "ksp",
            ).dependencies.add(dependencyFactory.create("app.softwork.cikraft:ksp-plugin:$VERSION"))

            configurations.consumable("cikraftEntryPoints") { cikraftEntryPoints ->
                cikraftEntryPoints.attributes {
                    it.attribute(Usage.USAGE_ATTRIBUTE, it.named(Usage::class.java, SAPCI_USAGE))
                    it.attribute(SAPCI.attribute, it.named(SAPCI::class.java, SAPCI.JSON_ENTRYPOINTS))
                }
                cikraftEntryPoints.outgoing {
                    val lazyArtifact = objects.newInstance(
                        LazyDirectoryArtifact::class.java,
                        "json",
                        "entrypoints",
                        "kspKotlin",
                        layout.contextBuildDirectory.map {
                            it.file(
                                "generated/ksp/main/resources/cikraft/entrypoint.json",
                            )
                        },
                    )
                    it.artifact(lazyArtifact)
                }
            }

            val mainCompilationUnit = parentBuildModel.compilationUnits.getByName("main")

            val mainJvmEcosystem = mainCompilationUnit.jvmEcosystem
            mainJvmEcosystem.implementationConfiguration.dependencies.add(
                dependencyFactory.create("app.softwork.cikraft:runtime:$VERSION"),
            )
            mainJvmEcosystem.compileOnlyConfiguration.dependencies.add(dependencyFactory.create(SAPCI_SCRIPT_API))
            mainJvmEcosystem.compileOnlyConfiguration.dependencies.add(dependencyFactory.create(SAPCI_GENERIC_API))

            tasks.named("processResources", ProcessResources::class.java) {
                it.exclude("cikraft/entrypoint.json")
            }

            buildModel as DefaultSAPCIGeneratorBuildModel
            buildModel.internalName = ""
            buildModel.sourceDirectorySet = mainCompilationUnit.sources
        }
    }
}

public interface CiKraftBaseDefinition : SAPCIGeneratorDefinition
