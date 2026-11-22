package app.softwork.cikraft.gradle

import org.gradle.api.*
import org.gradle.api.credentials.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.*
import org.gradle.workers.*
import javax.inject.*
import kotlin.String

@UntrackedTask(because = "Create infrastructure is a remote operation")
public abstract class ApiProxiesTask(stageName: String) : DefaultTask() {
    init {
        group = "cikraft"
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input
    internal abstract val httpSuffix: Property<String>

    @get:Input
    internal abstract val apiPortalServer: Property<String>

    @get:Input
    internal abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class.java,
        "${SAP_CI_CREDENTIALS}Api$stageName",
    )

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor
}

@UntrackedTask(because = "Create infrastructure is a remote operation")
public abstract class DeployApiProxiesTask @Inject constructor(private val apiName: String, stageName: String) :
    ApiProxiesTask(stageName) {
    @get:ServiceReference("apiTransportParallelService")
    internal abstract val apiTransportParallelBuildService: Property<ApiTransportParallelBuildService>

    @get:Input
    internal abstract val virtualHostId: Property<String>

    @TaskAction
    internal fun deploy() {
        workerExecutor.processIsolation {
            it.classpath.from(workerClasspath)
        }.submit(CreateApiProxiesWorker::class.java) {
            it.apiTransportParallelBuildService.set(apiTransportParallelBuildService)
            it.apiName.set(apiName)
            it.httpSuffix.set(httpSuffix)
            it.apiPortalServer.set(apiPortalServer)
            it.apiPortalClientId.set(credentials.map { it.username })
            it.apiPortalClientSecret.set(credentials.map { it.password })
            it.authServer.set(authServer)
            it.virtualHostId.set(virtualHostId)
        }
    }
}

@UntrackedTask(because = "Create infrastructure is a remote operation")
public abstract class UnDeployApiProxiesTask @Inject constructor(stageName: String) : ApiProxiesTask(stageName) {
    @TaskAction
    internal fun deploy() {
        workerExecutor.processIsolation {
            it.classpath.from(workerClasspath)
        }.submit(DeleteApiProxiesWorker::class.java) {
            it.httpSuffix.set(httpSuffix)
            it.apiPortalServer.set(apiPortalServer)
            it.apiPortalClientId.set(credentials.map { it.username })
            it.apiPortalClientSecret.set(credentials.map { it.password })
            it.authServer.set(authServer)
        }
    }
}
