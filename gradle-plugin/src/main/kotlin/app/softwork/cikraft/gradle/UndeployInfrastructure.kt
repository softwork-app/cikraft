package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.*
import org.gradle.api.*
import org.gradle.api.credentials.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.*
import org.gradle.kotlin.dsl.*
import org.gradle.workers.*
import javax.inject.*
import org.gradle.api.logging.Logging as GradleLogging

@UntrackedTask(because = "Deleting is a remote operation.")
public abstract class UndeployInfrastructure : DefaultTask() {
    init {
        group = "cikraft"
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input
    internal abstract val stageName: Property<String>

    @get:Input
    internal abstract val apiServer: Property<String>

    @get:Input
    internal abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = stageName.flatMap { stageName ->
        project.providers.credentials(
            PasswordCredentials::class.java,
            SAP_CI_CREDENTIALS + stageName.replaceFirstChar { it.uppercase() },
        )
    }

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Input
    internal abstract val flowIds: SetProperty<String>

    @get:Option(option = "suffix", description = "")
    @get:Input
    @get:Optional
    public abstract val suffix: Property<String>

    @TaskAction
    protected fun deleteFlow() {
        workerExecutor.processIsolation {
            it.classpath.from(workerClasspath)
        }.submit(UndeployInfrastructureWorker::class.java) {
            it.apiServer.set(apiServer)
            it.authServer.set(authServer)
            it.clientID.set(credentials.map { it.username!! })
            it.clientSecret.set(credentials.map { it.password!! })
            it.flowIds.set(
                flowIds.zip(suffix.orElse("")) { flowIds, suffix ->
                    flowIds.map {
                        flowIdWithSuffix(it, suffix)
                    }
                },
            )
        }
    }
}

internal fun flowIdWithSuffix(flowID: String, suffix: String): String {
    val id = flowID.replace("_", "")
    return if (suffix.isBlank()) {
        id
    } else {
        val suffixID = suffix.replace("/", "").uppercase()
        "${id}$suffixID"
    }
}

internal abstract class UndeployInfrastructureWorker : WorkAction<UndeployInfrastructureWorker.Params> {
    interface Params : WorkParameters {
        val apiServer: Property<String>
        val authServer: Property<String>
        val clientID: Property<String>
        val clientSecret: Property<String>
        val flowIds: SetProperty<String>
    }

    override fun execute(): Unit = runBlocking {
        HttpClient(CIO) {
            setupSapCIApiClient(
                apiServer = parameters.apiServer.get(),
                authServer = parameters.authServer.get(),
                clientId = parameters.clientID.get(),
                clientSecret = parameters.clientSecret.get(),
            )
            Logging {
                setupGradleLogging(GradleLogging.getLogger(UndeployInfrastructureWorker::class.java))
            }
        }.use {
            for (flowID in parameters.flowIds.get()) {
                it.undeployIntegrationFlow(flowID)
                it.deleteIntegrationFlow(flowID)
            }
        }
    }
}
