package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.BuildModel
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

@BindsProjectFeature(OpenApiFeature::class)
abstract class OpenApiFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("openApi", ApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
    }

    abstract class ApplyAction : ProjectFeatureApplyAction<OpenApiDefinition, BuildModel.None, SAPCIIFlowsDefinition> {
        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        @get:Inject
        abstract val project: Project

        @get:Inject abstract val dependencyFactory: DependencyFactory

        @get:Inject abstract val softwareComponentFactory: SoftwareComponentFactory

        @get:Inject abstract val objectFactory: ObjectFactory

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: OpenApiDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIIFlowsDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)
            parentBuildModel as DefaultSAPCIIFlowsBuildModel
            val flowsFolder = configurations.resolvable("cikraftOpenApiCreatedFlows") {
                fromDependencyCollector(definition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.API))
                }
            }.flatMap { it.elements }.map { it.single().asFile }

            val sapCIWorkerGenerator = configurations.dependencyScope("cikraftOpenApiWorkerGenerator") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val sapCIWorkerGeneratorClasspath = configurations.resolvable("cikraftOpenApiWorkerGeneratorClasspath") {
                extendsFrom(sapCIWorkerGenerator)
            }

            val generateOpenApiToolsClasspath = configurations.resolvable(
                "cikraftGenerateOpenApiTransformersClasspath",
            ) {
                fromDependencyCollector(definition.dependencies.transformers)
            }

            val generateOpenApi = tasks.register("generateOpenApi", GenerateOpenApi::class.java) {
                workerClasspath.from(sapCIWorkerGeneratorClasspath)
                createdFlows.fileProvider(flowsFolder)
                transformers.from(generateOpenApiToolsClasspath)
                this.title.convention(definition.title)
                this.apiDescription.convention(definition.description)

                val serverUrls = parentBuildModel.openApiStages.elements.map { stages ->
                    stages.map { stage ->
                        objectFactory.newInstance<Server>().apply {
                            http.set(
                                stage.httpServer.zip(parentBuildModel.httpSuffix) { server, suffix -> server + suffix },
                            )
                            description.set(stage.description)
                        }
                    }
                }

                this.servers.addAll(serverUrls)
                this.tags.putAll(
                    parentBuildModel.integrationPackages.elements.map { integrationPackage ->
                        integrationPackage.associate { it.name to it.description.get() }
                    },
                )
            }

            val sapCIOpenApi = configurations.consumable("cikraftOpenApi") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.OPENAPI))
                }
                outgoing {
                    artifact(generateOpenApi)
                }
            }

            val component = softwareComponentFactory.adhoc("cikraft")
            component.addVariantsFromConfiguration(sapCIOpenApi) {}

            project.pluginManager.apply("maven-publish")

            val publishing = project.extensions.getByName("publishing") as PublishingExtension
            publishing.apply {
                publications.register<MavenPublication>("cikraft") {
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                    from(component)
                }
            }
        }
    }
}
