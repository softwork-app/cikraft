package app.softwork.cikraft.gradle

import org.gradle.api.*
import org.gradle.api.credentials.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.workers.*
import javax.inject.*
import kotlin.String

@UntrackedTask(because = "Create infrastructure is a remote operation")
abstract class ApiProxiesTask(stageName: String) : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:Input
    internal abstract val url: Property<String>

    @get:Input
    internal abstract val httpSuffix: Property<String>

    @get:Input
    internal abstract val apiPortalServer: Property<String>

    @get:Input
    internal abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class,
        "${SAP_CI_CREDENTIALS}Api$stageName",
    )

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor
}

@UntrackedTask(because = "Create infrastructure is a remote operation")
abstract class DeployApiProxiesTask @Inject constructor(stageName: String) : ApiProxiesTask(stageName) {
    @get:Input
    internal abstract val virtualHost: Property<String>

    @TaskAction
    internal fun deploy() {
        workerExecutor.processIsolation {
            classpath.from(workerClasspath)
        }.submit(CreateApiProxiesWorker::class) {
            this.url.set(this@DeployApiProxiesTask.url)
            this.httpSuffix.set(this@DeployApiProxiesTask.httpSuffix)
            this.apiPortalServer.set(this@DeployApiProxiesTask.apiPortalServer)
            this.apiPortalClientId.set(this@DeployApiProxiesTask.credentials.map { it.username })
            this.apiPortalClientSecret.set(this@DeployApiProxiesTask.credentials.map { it.password })
            this.authServer.set(this@DeployApiProxiesTask.authServer)
            this.virtualHost.set(this@DeployApiProxiesTask.virtualHost)
        }
    }
}

@UntrackedTask(because = "Create infrastructure is a remote operation")
abstract class UnDeployApiProxiesTask @Inject constructor(stageName: String) : ApiProxiesTask(stageName) {
    @TaskAction
    internal fun deploy() {
        workerExecutor.processIsolation {
            classpath.from(workerClasspath)
        }.submit(DeleteApiProxiesWorker::class) {
            this.url.set(this@UnDeployApiProxiesTask.url)
            this.httpSuffix.set(this@UnDeployApiProxiesTask.httpSuffix)
            this.apiPortalServer.set(this@UnDeployApiProxiesTask.apiPortalServer)
            this.apiPortalClientId.set(this@UnDeployApiProxiesTask.credentials.map { it.username })
            this.apiPortalClientSecret.set(this@UnDeployApiProxiesTask.credentials.map { it.password })
            this.authServer.set(this@UnDeployApiProxiesTask.authServer)
        }
    }
}
