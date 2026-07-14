package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.BuildAndDeployStatus
import app.softwork.cikraft.api.deployIntegrationFlow
import app.softwork.cikraft.api.getBuildAndDeployStatus
import app.softwork.cikraft.api.setupSapCIApiClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import org.gradle.api.logging.Logging as GradleLogger

@UntrackedTask(because = "Deploying a IFlow is a remote operation")
internal abstract class DeployIFlow @Inject constructor(
    @get:Input
    private val stageName: String,
) : DefaultTask() {
    init {
        group = "cikraft"
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input
    internal abstract val apiServer: Property<String>

    @get:Input
    internal abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class,
        SAP_CI_CREDENTIALS + stageName,
    )

    @get:Input
    internal abstract val flowID: Property<String>

    @get:Input
    internal abstract val version: Property<String>

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @TaskAction
    internal fun create() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(DeployIFlowWorker::class) {
            apiServer.set(this@DeployIFlow.apiServer)
            authServer.set(this@DeployIFlow.authServer)
            clientID.set(this@DeployIFlow.credentials.map { it.username })
            clientSecret.set(this@DeployIFlow.credentials.map { it.password })

            flowID.set(this@DeployIFlow.flowID)
            version.set(this@DeployIFlow.version)
        }
    }
}

internal abstract class DeployIFlowWorker : WorkAction<DeployIFlowWorker.Params> {
    interface Params : WorkParameters {
        val apiServer: Property<String>
        val authServer: Property<String>
        val clientID: Property<String>
        val clientSecret: Property<String>

        val flowID: Property<String>
        val version: Property<String>
    }

    override fun execute() {
        val version = parameters.version.get()
        val flowID = parameters.flowID.get()

        HttpClient(CIO) {
            setupSapCIApiClient(
                apiServer = parameters.apiServer.get(),
                authServer = parameters.authServer.get(),
                clientId = parameters.clientID.get(),
                clientSecret = parameters.clientSecret.get(),
            )
            Logging {
                setupGradleLogging(GradleLogger.getLogger(DeployIFlowWorker::class.java))
            }
        }.use { client ->
            runBlocking {
                val taskId = client.deployIntegrationFlow(flowID, version)
                requireNotNull(taskId) {
                    "Internal error, taskID was null, check IF $flowID with version $version"
                }
                while (true) {
                    val status = client.getBuildAndDeployStatus(taskId)
                    when (status.status) {
                        BuildAndDeployStatus.Status.Success -> break

                        BuildAndDeployStatus.Status.Fail -> error(
                            "Deployment failed for $flowID with status $status\n",
                        )

                        BuildAndDeployStatus.Status.Deploying -> delay(3.seconds)
                    }
                }
            }
        }
    }
}
