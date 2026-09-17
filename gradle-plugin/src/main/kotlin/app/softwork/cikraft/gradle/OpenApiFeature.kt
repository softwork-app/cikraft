package app.softwork.cikraft.gradle

import app.softwork.cikraft.gradle.apiproxies.GenerateOpenAPIProxyTransformer
import app.softwork.cikraft.gradle.apiproxies.GenerateOpenAPITransformerServiceLoader
import app.softwork.cikraft.gradle.apiproxies.Proxies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
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
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@BindsProjectFeature(OpenApiFeature::class)
abstract class OpenApiFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(target: Project) {}

    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("openApi", IFlowApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()

        builder.bindProjectFeature("openApi", ApiProxyApplyAction::class)
            .withUnsafeApplyAction()
            .withUnsafeDefinition()
    }

    abstract class IFlowApplyAction : ApplyAction<SAPCIIFlowsDefinition, SAPCIIFlowsBuildModel> {
        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: OpenApiDefinition,
            buildModel: BuildModel.None,
            parentDefinition: SAPCIIFlowsDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            val servers = parentBuildModel.stages.elements.map { stages ->
                stages.map { stage ->
                    objectFactory.newInstance<Server>().apply {
                        http.set(
                            stage.httpServer.zip(parentBuildModel.httpSuffix) { server, suffix -> server + suffix },
                        )
                        description.set(stage.description)
                    }
                }
            }

            apply(definition, "", null, SAPCI.OPENAPI).configure {
                this.servers.addAll(servers)
                openApiFile.convention(
                    layout.contextBuildDirectory.map {
                        it.file("cikraft/openapi.json")
                    },
                )
            }
        }
    }

    abstract class ApiProxyApplyAction : ApplyAction<APIProxiesDefinition, APIProxiesBuildModel> {
        @get:Inject
        abstract val sourceSets: SourceSetContainer

        @get:Inject abstract val allConfigurations: ConfigurationContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: OpenApiDefinition,
            buildModel: BuildModel.None,
            parentDefinition: APIProxiesDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            val apiProxiesOpenApiSourceSet = sourceSets.create("apiProxiesOpenApi")
            allConfigurations.named(apiProxiesOpenApiSourceSet.implementationConfigurationName) {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }

            val s = tasks.register(
                "generateOpenAPITransformerServiceLoader",
                GenerateOpenAPITransformerServiceLoader::class.java,
            ) {
                outputDirectory.set(
                    layout.contextBuildDirectory.map {
                        it.dir("generated/cikraft/apiProxies/resources")
                    },
                )
            }

            apiProxiesOpenApiSourceSet.resources.srcDir(s)

            val sapCIWorkerGenerator = configurations.dependencyScope("cikraftOpenApiProxyWorkerGenerator") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val sapCIWorkerGeneratorClasspath =
                configurations.resolvable("cikraftOpenApiProxyWorkerGeneratorClasspath$sapCIWorkerGenerator") {
                    extendsFrom(sapCIWorkerGenerator)
                }

            val generateTransformer =
                tasks.register("generateOpenAPIProxyTransformer", GenerateOpenAPIProxyTransformer::class.java) {
                    this.apiProxies.addAllLater(
                        parentBuildModel.apiProxies.elements.map {
                            it.map { apiProxy ->
                                objectFactory.newInstance(Proxies::class.java, apiProxy.name).apply {
                                    val apiHosts = parentBuildModel.stages.elements.zip(
                                        apiProxy.virtualHostName,
                                    ) { stages, virtualHostName ->
                                        val s = objectFactory.setProperty(String::class.java)
                                        for (stage in stages) {
                                            s.add(
                                                stage.apiVirtualHosts.named(
                                                    virtualHostName,
                                                ).flatMap { it.apiHttpServer },
                                            )
                                        }
                                        s
                                    }.flatMap { it }
                                    this.apiHosts.addAll(apiHosts)

                                    val s = parentBuildModel.stages.elements.zip(
                                        apiProxy.virtualHostName,
                                    ) { stages, virtualHostName ->
                                        val s = objectFactory.property(Boolean::class.java)
                                        for (stage in stages) {
                                            s.set(
                                                stage.apiVirtualHosts.named(
                                                    virtualHostName,
                                                ).flatMap { it.clientAuthEnabled },
                                            )
                                        }
                                        s
                                    }.flatMap { it }
                                    this.clientAuthEnabled.set(s)
                                }
                            }
                        },
                    )
                    this.httpSuffix.set(parentBuildModel.httpSuffix)
                    workerClasspath.from(
                        parentBuildModel.classes,
                        parentBuildModel.runtimeClasspath,
                        sapCIWorkerGeneratorClasspath,
                    )
                    outputDirectory.set(
                        layout.contextBuildDirectory.map {
                            it.dir(
                                "generated/cikraft/apiproxies/openapi/kotlin/apiProxiesOpenApi",
                            )
                        },
                    )
                }

            apiProxiesOpenApiSourceSet.kotlin.srcDir(generateTransformer)

            val generateOpenAPITask = apply(definition, "apiProxy", "api-proxy", SAPCI.OPENAPI_PROXY)
            generateOpenAPITask.configure {
                openApiFile.convention(
                    layout.contextBuildDirectory.map {
                        it.file("cikraft/openapi-apiproxy.json")
                    },
                )
                this.transformers.from(apiProxiesOpenApiSourceSet.output)
            }
        }
    }

    interface ApplyAction<D : Definition<B>, B : BuildModel> :
        ProjectFeatureApplyAction<OpenApiDefinition, BuildModel.None, D> {
        @get:Inject
        val configurations: ConfigurationRegistrar

        @get:Inject
        val tasks: TaskRegistrar

        @get:Inject
        val layout: ProjectFeatureLayout

        @get:Inject
        val dependencyFactory: DependencyFactory

        @get:Inject
        val components: SoftwareComponentContainer

        @get:Inject
        val objectFactory: ObjectFactory

        fun apply(
            definition: OpenApiDefinition,
            suffix: String,
            classifier: String?,
            sapciAttribute: String,
        ): TaskProvider<GenerateOpenApi> {
            val flowsFolder = configurations.resolvable("cikraftOpenApiCreatedFlows$suffix") {
                fromDependencyCollector(definition.dependencies.infrastructure)
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(SAPCI.API))
                }
            }

            val sapCIWorkerGenerator = configurations.dependencyScope("cikraftOpenApiWorkerGenerator$suffix") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val sapCIWorkerGeneratorClasspath =
                configurations.resolvable("cikraftOpenApiWorkerGeneratorClasspath$suffix") {
                    extendsFrom(sapCIWorkerGenerator)
                }

            val generateOpenApiToolsClasspath = configurations.resolvable(
                "cikraftGenerateOpenApiTransformersClasspath$suffix",
            ) {
                fromDependencyCollector(definition.dependencies.transformers)
            }

            val generateOpenApi = tasks.register("generateOpenApi$suffix", GenerateOpenApi::class.java) {
                workerClasspath.from(sapCIWorkerGeneratorClasspath)
                createdFlows.setFrom(flowsFolder)
                transformers.from(generateOpenApiToolsClasspath)
                this.title.convention(definition.title)
                this.apiDescription.convention(definition.description)
            }

            val sapCIOpenApi = configurations.consumable("cikraftOpenApi$suffix") {
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, named(SAPCI_USAGE))
                    attribute(SAPCI.attribute, named(sapciAttribute))
                }
                outgoing {
                    artifact(generateOpenApi) {
                        this.classifier = classifier
                    }
                }
            }

            val component = components.getByName("java") as AdhocComponentWithVariants
            component.addVariantsFromConfiguration(sapCIOpenApi) {}

            return generateOpenApi
        }
    }
}
