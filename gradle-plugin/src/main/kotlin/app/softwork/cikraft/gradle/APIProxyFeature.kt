package app.softwork.cikraft.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.features.annotations.BindsProjectFeature
import org.gradle.features.binding.ProjectFeatureApplicationContext
import org.gradle.features.binding.ProjectFeatureApplyAction
import org.gradle.features.binding.ProjectFeatureBinding
import org.gradle.features.binding.ProjectFeatureBindingBuilder
import org.gradle.features.dsl.bindProjectFeature
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
    }

    abstract class ApplyAction :
        ProjectFeatureApplyAction<APIProxiesDefinition, APIProxiesBuildModel, SAPCIInfrastructureDefinition> {
        @get:Inject
        abstract val tasks: TaskRegistrar

        @get:Inject abstract val configurations: ConfigurationRegistrar

        @get:Inject abstract val configurationContainer: ConfigurationContainer

        @get:Inject abstract val dependencyFactory: DependencyFactory

        @get:Inject abstract val sourceSets: SourceSetContainer

        override fun apply(
            context: ProjectFeatureApplicationContext,
            definition: APIProxiesDefinition,
            buildModel: APIProxiesBuildModel,
            parentDefinition: SAPCIInfrastructureDefinition,
        ) {
            buildModel.apiCertificates.addAll(definition.apiCertificates)
            buildModel.apiKeyStores.addAll(definition.apiKeyStores)
            buildModel.apiRuntimeProviders.addAll(definition.apiRuntimeProviders)

            val parentBuildModel = context.getBuildModel(parentDefinition)

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

            val apiProxySourceSet = sourceSets.create("apiProxies") {
                configurationContainer.named(implementationConfigurationName) {
                    dependencies.add(
                        dependencyFactory.create("app.softwork.cikraft:integration-flow-builder-runtime:$VERSION"),
                    )
                }
            }

            parentBuildModel.apiStages.all {
                val stage = this
                val taskName = stage.name.replaceFirstChar { it.uppercase() }

                buildModel.apiCertificates.all {
                    val apiCertificate = this
                    tasks.register(
                        "createApiCertificate${name}On${stage.name}",
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

                stage.apiVirtualHosts.all {
                    val virtualHost = this
                    tasks.register(
                        "deploy${taskName}ApiTo${virtualHost.name}ApiHost",
                        DeployApiProxiesTask::class.java,
                        stage.name,
                    ).configure {
                        this.url.set(stage.httpServer)
                        this.httpSuffix.set(parentBuildModel.httpSuffix)
                        this.apiPortalServer.set(stage.apiPortalServer)
                        this.authServer.set(stage.authServer)
                        this.virtualHostId.set(virtualHost.id)

                        workerClasspath.from(
                            apiProxySourceSet.kotlin.classesDirectory,
                            apiProxySourceSet.runtimeClasspath,
                            apiWorkerClasspath,
                        )
                    }
                }
                tasks.register(
                    "undeploy${taskName}Api",
                    UnDeployApiProxiesTask::class.java,
                    stage.name,
                ).configure {
                    this.url.set(stage.httpServer)
                    this.httpSuffix.set(parentBuildModel.httpSuffix)
                    this.apiPortalServer.set(stage.apiPortalServer)
                    this.authServer.set(stage.authServer)

                    workerClasspath.from(
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

                        workerClasspath.from(apiWorkerClasspath)
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

                        workerClasspath.from(apiWorkerClasspath)
                    }
                }
            }
        }
    }
}
