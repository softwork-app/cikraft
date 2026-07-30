package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.declarative.projecttypes.jvmapplication.JvmApplicationProjectType
import javax.inject.Inject

@BindsProjectFeature(InfrastructureFeature::class)
abstract class InfrastructureFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("ciKraftInfrastructure", ApplyAction::class)
            .withBuildModelImplementationType(DefaultSAPCIInfrastructureBuildModel::class.java)
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<SAPCIInfrastructureDefinition, SAPCIInfrastructureBuildModel, JvmApplicationProjectType> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject abstract val tasks: TaskRegistrar

        @get:Inject abstract val layout: ProjectFeatureLayout

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: SAPCIInfrastructureDefinition,
            buildModel: SAPCIInfrastructureBuildModel,
            parentDefinition: JvmApplicationProjectType,
        ) {
            buildModel as DefaultSAPCIInfrastructureBuildModel
            buildModel.suffix.set(definition.suffix)
            buildModel.httpSuffix.set(definition.httpNamespace.zip(buildModel.suffix.orElse("")) { a, b -> "$a$b" })

            buildModel.apiStages.addAll(definition.apiStages)
            buildModel.transportStages.addAll(definition.transportStages)

            val parentBuildModel = context.getBuildModel(parentDefinition)
            buildModel.compilationUnits = parentBuildModel.compilationUnits

            // workaround until Gradle provider migration for Test.environment
            val writeStages = tasks.register("writeStages", WriteStages::class.java) {
                this.stagesUrls.set(
                    buildModel.apiStages.associate {
                        it.name + "_HTTP" to it.httpServer.get()
                    } + buildModel.apiStages.associate {
                        it.name + "_API" to it.apiVirtualHosts.joinToString { it.apiHttpServer.get() }
                    } + buildModel.transportStages.associate {
                        it.name + "_HTTP" to it.httpServer.get()
                    } + buildModel.transportStages.associate {
                        it.name + "_API" to it.apiVirtualHosts.joinToString { it.apiHttpServer.get() }
                    },
                )
                this.httpSuffix.set(buildModel.httpSuffix)
                this.output.set(layout.contextBuildDirectory.map { it.file("cikraft/stages.properties") })
            }

            configurations.consumable("apiStages") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.STAGES))
                }
                outgoing {
                    artifact(writeStages)
                }
            }
        }
    }
}
