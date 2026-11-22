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
public abstract class APIProxyFeature :
    Plugin<Project>,
    ProjectFeatureBinding {
    override fun apply(project: Project) {}
    override fun bind(builder: ProjectFeatureBindingBuilder) {
        builder.bindProjectFeature("apiProxies", ApplyAction::class)
            .withUnsafeApplyAction()
            .withBuildModelImplementationType(DefaultAPIProxiesBuildModel::class.java)
            .withNestedBuildModelImplementationType(
                ApiKeyStoreBuildModel::class.java,
                DefaultApiKeyStoreBuildModel::class.java,
            )
            .withNestedBuildModelImplementationType(
                ApiCertificateBuildModel::class.java,
                DefaultApiCertificateBuildModel::class.java,
            )
            .withNestedBuildModelImplementationType(
                ApiRuntimeProviderBuildModel::class.java,
                DefaultApiRuntimeProviderBuildModel::class.java,
            )
            .withNestedBuildModelImplementationType(
                ApiProxyBuildModel::class.java,
                DefaultApiProxyBuildModel::class.java,
            )
    }

    public abstract class ApplyAction :
        ProjectFeatureApplyAction<APIProxiesDefinition, APIProxiesBuildModel, SAPCIInfrastructureDefinition> {
        @get:Inject
        protected abstract val tasks: TaskRegistrar

        @get:Inject
        protected abstract val configurations: ConfigurationRegistrar

        @get:Inject
        protected abstract val layout: ProjectFeatureLayout

        @get:Inject
        protected abstract val objectFactory: ObjectFactory

        @get:Inject
        protected abstract val configurationContainer: ConfigurationContainer

        @get:Inject
        protected abstract val dependencyFactory: DependencyFactory

        @get:Inject
        protected abstract val sourceSets: SourceSetContainer

        @get:Inject
        protected abstract val gradle: Gradle

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: APIProxiesDefinition,
            buildModel: APIProxiesBuildModel,
            parentDefinition: SAPCIInfrastructureDefinition,
        ) {
            val parentBuildModel = context.getBuildModel(parentDefinition)

            buildModel as DefaultAPIProxiesBuildModel
            buildModel.apiCertificates.addAllLater(
                definition.apiCertificates.elements.map {
                    it.map {
                        val buildModel = context.getBuildModel(it)
                        buildModel as DefaultApiCertificateBuildModel
                        buildModel.name = it.name
                        buildModel.p12File.set(it.p12File)
                        buildModel.storeName.set(it.storeName)
                        buildModel.description.set(it.description)
                        buildModel
                    }
                },
            )
            buildModel.apiKeyStores.addAllLater(
                definition.apiKeyStores.elements.map {
                    it.map {
                        val buildModel = context.getBuildModel(it)
                        buildModel as DefaultApiKeyStoreBuildModel
                        buildModel.name = it.name
                        buildModel
                    }
                },
            )
            buildModel.apiRuntimeProviders.addAllLater(
                definition.apiRuntimeProviders.elements.map {
                    it.map {
                        val buildModel = context.getBuildModel(it)
                        buildModel as DefaultApiRuntimeProviderBuildModel
                        buildModel.name = it.name
                        buildModel.title.set(it.title)
                        buildModel.credentialStoreName.set(it.credentialStoreName)
                        buildModel.credentialName.set(it.credentialName)
                        buildModel
                    }
                },
            )
            buildModel.apiProxies.addAllLater(
                definition.apiProxies.elements.map {
                    it.map {
                        val buildModel = context.getBuildModel(it)
                        buildModel as DefaultApiProxyBuildModel
                        buildModel.name = it.name // https://github.com/gradle/gradle/issues/38207
                        buildModel.description.set(it.description)
                        buildModel.virtualHostName.set(it.virtualHostName)
                        buildModel
                    }
                },
            )
            buildModel.httpSuffix.set(parentBuildModel.httpSuffix)
            buildModel.stages.addAllLater(parentBuildModel.apiStages.elements)
            buildModel.stages.addAllLater(parentBuildModel.transportStages.elements)

            val deps = configurations.dependencyScope("apiProxyWorker") {
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:api:$VERSION"))
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:core:$VERSION"))
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:flow-dsl:$VERSION"))
                it.dependencies.add(dependencyFactory.create(KTOR_CIO))
                it.dependencies.add(dependencyFactory.create(KTOR_LOGGING))
            }

            val apiWorkerClasspath = configurations.resolvable("apiProxyWorkerClasspath") {
                it.extendsFrom(deps)
            }

            definition.dependencies.implementation.add("app.softwork.cikraft:api-proxies-builder-runtime:$VERSION")

            val apiProxySourceSet = sourceSets.create("apiProxies") {
                configurationContainer.named(it.implementationConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.implementation)
                }
                configurationContainer.named(it.runtimeOnlyConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.runtimeOnly)
                }
                configurationContainer.named(it.compileOnlyConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.compileOnly)
                }
                configurationContainer.named(it.annotationProcessorConfigurationName) {
                    it.fromDependencyCollector(definition.dependencies.annotationProcessor)
                }
            }

            buildModel.classes.from(apiProxySourceSet.kotlin.classesDirectory)
            buildModel.runtimeClasspath.from(apiProxySourceSet.runtimeClasspath)

            val workerDeps = configurations.dependencyScope("cikraftGenerateTypesafeWorker") {
                it.dependencies.add(dependencyFactory.create("app.softwork.cikraft:generator:$VERSION"))
            }
            val workerClasspath = configurations.resolvable("cikraftGenerateTypesafeWorkerClasspath") {
                it.extendsFrom(workerDeps)
            }

            val generateTypesafeApiRuntimeProviders =
                tasks.register("generateTypesafeApiRuntimeProviders", GenerateTypeApiRuntimeProviders::class.java) {
                    it.apiRuntimeProviders.addAllLater(
                        buildModel.apiRuntimeProviders.elements.map {
                            it.map { apiRuntimeProvider ->
                                objectFactory.newInstance(
                                    GenerateTypeApiRuntimeProviders.ApiRuntimeProvider::class.java,
                                    apiRuntimeProvider.name,
                                ).apply {
                                    description.set(apiRuntimeProvider.title)
                                }
                            }
                        },
                    )
                    it.outputDirectory.set(
                        layout.contextBuildDirectory.map {
                            it.dir("generated/cikraft/apiProxies/kotlin/apiruntimeproviders")
                        },
                    )
                    it.workerClasspath.from(workerClasspath)
                }
            apiProxySourceSet.kotlin.srcDir(generateTypesafeApiRuntimeProviders)

            parentBuildModel.apiStages.all { stage ->
                val stageTaskName = stage.name.replaceFirstChar { it.uppercase() }

                buildModel.apiCertificates.all { apiCertificate ->
                    tasks.register(
                        "createApiCertificate${apiCertificate.name}On$stageTaskName",
                        CreateApiCertificateTask::class.java,
                        stage.name,
                    ).configure {
                        it.workerClasspath.from(apiWorkerClasspath)
                        it.apiPortalServer.set(stage.apiPortalServer)
                        it.authServer.set(stage.authServer)
                        it.apiCertificateName.set(apiCertificate.name)
                        it.apiCertificateDescription.set(apiCertificate.description)
                        it.apiCertificateP12File.set(apiCertificate.p12File)
                        it.apiCertificateStoreName.set(apiCertificate.storeName)
                    }
                    tasks.register(
                        "deleteApiCertificate${apiCertificate.name}On$stageTaskName",
                        DeleteApiCertificateTask::class.java,
                        stage.name,
                        apiCertificate.name,
                    ).configure {
                        it.workerClasspath.from(apiWorkerClasspath)
                        it.apiPortalServer.set(stage.apiPortalServer)
                        it.authServer.set(stage.authServer)
                        it.storeName.set(apiCertificate.storeName)
                    }
                }

                val deployApiTask = tasks.register("deploy${stageTaskName}Api")

                gradle.sharedServices.registerIfAbsent(
                    "apiTransportParallelService",
                    ApiTransportParallelBuildService::class.java,
                ) {
                    it.maxParallelUsages.set(1)
                }

                buildModel.apiProxies.all { apiProxyBuildModel ->
                    val deployApiToHostTask = tasks.register(
                        "deploy${apiProxyBuildModel.name}To$stageTaskName",
                        DeployApiProxiesTask::class.java,
                        apiProxyBuildModel.name,
                        stage.name,
                    )
                    deployApiToHostTask.configure {
                        it.httpSuffix.set(parentBuildModel.httpSuffix)
                        it.apiPortalServer.set(stage.apiPortalServer)
                        it.authServer.set(stage.authServer)
                        it.virtualHostId.set(
                            apiProxyBuildModel.virtualHostName.flatMap { stage.apiVirtualHosts.named(it) }
                                .flatMap { it.id },
                        )

                        it.workerClasspath.from(
                            buildModel.classes,
                            buildModel.runtimeClasspath,
                            apiWorkerClasspath,
                        )
                    }
                    deployApiTask.configure {
                        it.dependsOn(deployApiToHostTask)
                    }
                }

                tasks.register(
                    "undeploy${stageTaskName}Api",
                    UnDeployApiProxiesTask::class.java,
                    stage.name,
                ).configure {
                    it.httpSuffix.set(parentBuildModel.httpSuffix)
                    it.apiPortalServer.set(stage.apiPortalServer)
                    it.authServer.set(stage.authServer)

                    it.workerClasspath.from(
                        apiProxySourceSet.kotlin.classesDirectory,
                        apiProxySourceSet.runtimeClasspath,
                        apiWorkerClasspath,
                    )
                }

                buildModel.apiRuntimeProviders.all { apiRuntimeProvider ->
                    tasks.register(
                        "deployApiRuntimeProvider${apiRuntimeProvider.name}On$stageTaskName",
                        CreateApiRuntimeProviderTask::class.java,
                        stage.name,
                    ).configure {
                        it.apiPortalServer.set(stage.apiPortalServer)
                        it.authServer.set(stage.authServer)
                        it.httpServer.set(stage.httpServer)

                        it.providerName.set(apiRuntimeProvider.name)
                        it.providerTitle.set(apiRuntimeProvider.title)
                        it.providerCredentialName.set(apiRuntimeProvider.credentialName)
                        it.providerCredentialStoreName.set(apiRuntimeProvider.credentialStoreName)

                        it.workerClasspath.from(apiWorkerClasspath)
                    }
                }

                buildModel.apiKeyStores.all { apiKeyStore ->
                    tasks.register(
                        "deleteApiKeyStore${apiKeyStore.name}On$stageTaskName",
                        DeleteApiKeyStoreTask::class.java,
                        stage.name,
                    ).configure {
                        it.apiPortalServer.set(stage.apiPortalServer)
                        it.authServer.set(stage.authServer)
                        it.apiKeyStoreName.set(apiKeyStore.name)

                        it.workerClasspath.from(apiWorkerClasspath)
                    }
                }
            }
        }
    }
}
