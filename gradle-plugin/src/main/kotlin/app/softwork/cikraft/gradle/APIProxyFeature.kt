package app.softwork.cikraft.gradle

import app.softwork.cikraft.gradle.apiproxies.GenerateTypeApiRuntimeProviders
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
import org.gradle.features.file.ProjectFeatureLayout
import org.gradle.features.registration.ConfigurationRegistrar
import org.gradle.features.registration.TaskRegistrar
import javax.inject.Inject

@BindsProjectFeature(APIProxyFeature::class)
abstract class APIProxyFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("apiProxies", ApplyAction::class)
            .withUnsafeApplyAction()
            .withBuildModelImplementationType(DefaultAPIProxiesBuildModel::class.java)
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<APIProxiesDefinition, APIProxiesBuildModel, SAPCIInfrastructureDefinition> {
        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject
        abstract val configurations: ConfigurationRegistrar

        @get:Inject
        abstract val layout: ProjectFeatureLayout

        @get:Inject
        abstract val objectFactory: ObjectFactory

        @get:Inject
        abstract val configurationContainer: ConfigurationContainer

        @get:Inject
        abstract val dependencyFactory: DependencyFactory

        @get:Inject
        abstract val sourceSets: SourceSetContainer

        @get:Inject
        abstract val gradle: Gradle

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: APIProxiesDefinition,
            buildModel: APIProxiesBuildModel,
            parentDefinition: SAPCIInfrastructureDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            buildModel as DefaultAPIProxiesBuildModel
            buildModel.apiCertificates.addAll(definition.apiCertificates)
            buildModel.apiKeyStores.addAll(definition.apiKeyStores)
            buildModel.apiRuntimeProviders.addAll(definition.apiRuntimeProviders)
            buildModel.apiProxies.addAll(definition.apiProxies)
            buildModel.httpSuffix.set(parentBuildModel.httpSuffix)
            buildModel.stages.addAllLater(parentBuildModel.apiStages.elements)
            buildModel.stages.addAllLater(parentBuildModel.transportStages.elements)

            val deps = configurations.dependencyScope("apiProxyWorker") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:api:$VERSION"))
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:core:$VERSION"))
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:flow-dsl:$VERSION"))
                dependencies.add(dependencyFactory.create(KTOR_CIO))
                dependencies.add(dependencyFactory.create(KTOR_LOGGING))
            }

            val apiWorkerClasspath = configurations.resolvable("apiProxyWorkerClasspath") {
                extendsFrom(deps)
            }

            definition.dependencies.implementation.add("app.softwork.cikraft:api-proxies-builder-runtime:$VERSION")

            val apiProxySourceSet = sourceSets.create("apiProxies") {
                configurationContainer.named(implementationConfigurationName) {
                    fromDependencyCollector(definition.dependencies.implementation)
                }
                configurationContainer.named(runtimeOnlyConfigurationName) {
                    fromDependencyCollector(definition.dependencies.runtimeOnly)
                }
                configurationContainer.named(compileOnlyConfigurationName) {
                    fromDependencyCollector(definition.dependencies.compileOnly)
                }
                configurationContainer.named(annotationProcessorConfigurationName) {
                    fromDependencyCollector(definition.dependencies.annotationProcessor)
                }
            }

            buildModel.classes.from(apiProxySourceSet.kotlin.classesDirectory)
            buildModel.runtimeClasspath.from(apiProxySourceSet.runtimeClasspath)

            val workerDeps = configurations.dependencyScope("cikraftGenerateTypesafeWorker") {
                dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val workerClasspath = configurations.resolvable("cikraftGenerateTypesafeWorkerClasspath") {
                extendsFrom(workerDeps)
            }

            val generateTypesafeApiRuntimeProviders =
                tasks.register("generateTypesafeApiRuntimeProviders", GenerateTypeApiRuntimeProviders::class.java) {
                    apiRuntimeProviders.addAllLater(
                        buildModel.apiRuntimeProviders.elements.map {
                            it.map {
                                objectFactory.newInstance(
                                    GenerateTypeApiRuntimeProviders.ApiRuntimeProvider::class.java,
                                    it.name,
                                )
                                    .apply {
                                        description.set(it.title)
                                    }
                            }
                        },
                    )
                    outputDirectory.set(
                        layout.contextBuildDirectory.map {
                            it.dir("generated/cikraft/apiProxies/kotlin/apiruntimeproviders")
                        },
                    )
                    this.workerClasspath.from(workerClasspath)
                }
            apiProxySourceSet.kotlin.srcDir(generateTypesafeApiRuntimeProviders)

            parentBuildModel.apiStages.all {
                val stage = this
                val stageTaskName = stage.name.replaceFirstChar { it.uppercase() }

                buildModel.apiCertificates.all {
                    val apiCertificate = this
                    tasks.register(
                        "createApiCertificate${name}On$stageTaskName",
                        CreateApiCertificateTask::class.java,
                        stage.name,
                    ).configure {
                        this.workerClasspath.from(apiWorkerClasspath)
                        this.apiPortalServer.set(stage.apiPortalServer)
                        this.authServer.set(stage.authServer)
                        this.apiCertificateName.set(apiCertificate.name)
                        this.apiCertificateDescription.set(apiCertificate.description)
                        this.apiCertificateP12File.set(apiCertificate.p12File)
                        this.apiCertificateStoreName.set(apiCertificate.storeName)
                    }
                    tasks.register(
                        "deleteApiCertificate${name}On${stage.name}",
                        DeleteApiCertificateTask::class.java,
                        stage.name,
                        apiCertificate.name,
                    ).configure {
                        this.workerClasspath.from(apiWorkerClasspath)
                        this.apiPortalServer.set(stage.apiPortalServer)
                        this.authServer.set(stage.authServer)
                        this.storeName.set(apiCertificate.storeName)
                    }
                }

                val deployApiTask = tasks.register("deploy${stageTaskName}Api")

                gradle.sharedServices.registerIfAbsent(
                    "apiTransportParallelService",
                    ApiTransportParallelBuildService::class.java,
                ) {
                    maxParallelUsages.set(1)
                }

                buildModel.apiProxies.all {
                    val deployApiToHostTask = tasks.register(
                        "deploy${name}To$stageTaskName",
                        DeployApiProxiesTask::class.java,
                        name,
                        stage.name,
                    )
                    deployApiToHostTask.configure {
                        this.httpSuffix.set(parentBuildModel.httpSuffix)
                        this.apiPortalServer.set(stage.apiPortalServer)
                        this.authServer.set(stage.authServer)
                        this.virtualHostId.set(
                            virtualHostName.flatMap { stage.apiVirtualHosts.named(it) }
                                .flatMap { it.id },
                        )

                        this.workerClasspath.from(
                            buildModel.classes,
                            buildModel.runtimeClasspath,
                            apiWorkerClasspath,
                        )
                    }
                    deployApiTask.configure {
                        dependsOn(deployApiToHostTask)
                    }
                }

                tasks.register(
                    "undeploy${stageTaskName}Api",
                    UnDeployApiProxiesTask::class.java,
                    stage.name,
                ).configure {
                    this.httpSuffix.set(parentBuildModel.httpSuffix)
                    this.apiPortalServer.set(stage.apiPortalServer)
                    this.authServer.set(stage.authServer)

                    this.workerClasspath.from(
                        apiProxySourceSet.kotlin.classesDirectory,
                        apiProxySourceSet.runtimeClasspath,
                        apiWorkerClasspath,
                    )
                }

                buildModel.apiRuntimeProviders.all {
                    val apiRuntimeProvider = this
                    tasks.register(
                        "deployApiRuntimeProvider${apiRuntimeProvider.name}On${stage.name}",
                        CreateApiRuntimeProviderTask::class.java,
                        stage.name,
                    ).configure {
                        this.apiPortalServer.set(stage.apiPortalServer)
                        this.authServer.set(stage.authServer)
                        this.httpServer.set(stage.httpServer)

                        this.providerName.set(apiRuntimeProvider.name)
                        this.providerTitle.set(apiRuntimeProvider.title)
                        this.providerCredentialName.set(apiRuntimeProvider.credentialName)
                        this.providerCredentialStoreName.set(apiRuntimeProvider.credentialStoreName)

                        this.workerClasspath.from(apiWorkerClasspath)
                    }
                }

                buildModel.apiKeyStores.all {
                    val apiKeyStore = this
                    tasks.register(
                        "deleteApiKeyStore${apiKeyStore.name}On${stage.name}",
                        DeleteApiKeyStoreTask::class.java,
                        stage.name,
                    ).configure {
                        this.apiPortalServer.set(stage.apiPortalServer)
                        this.authServer.set(stage.authServer)
                        this.apiKeyStoreName.set(apiKeyStore.name)

                        this.workerClasspath.from(apiWorkerClasspath)
                    }
                }
            }
        }
    }
}
