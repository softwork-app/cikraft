package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.*
import app.softwork.cikraft.api.proxy.deleteCredentialsStore
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.gradle.api.*
import org.gradle.api.credentials.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.workers.*
import javax.inject.*
import org.gradle.api.logging.Logging as GradleLogger

@UntrackedTask(because = "Deleting an API keystore is a remote operation")
abstract class DeleteApiKeyStoreTask @Inject constructor(stageName: String) : DefaultTask() {
    init {
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input abstract val apiKeyStoreName: Property<String>

    @get:Input
    abstract val apiPortalServer: Property<String>

    @get:Input
    abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class,
        "${SAP_CI_CREDENTIALS}Api$stageName",
    )

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun deleteApiKeyStore() {
        workerExecutor.processIsolation {
            this.classpath.from(workerClasspath)
        }.submit(DeleteApiKeyStoreWorker::class) {
            this.apiPortalServer.set(this@DeleteApiKeyStoreTask.apiPortalServer)
            this.apiPortalClientId.set(this@DeleteApiKeyStoreTask.credentials.map { it.username })
            this.apiPortalClientSecret.set(this@DeleteApiKeyStoreTask.credentials.map { it.password })
            this.authServer.set(this@DeleteApiKeyStoreTask.authServer)
            this.name.set(this@DeleteApiKeyStoreTask.apiKeyStoreName)
        }
    }
}

internal abstract class DeleteApiKeyStoreWorker : WorkAction<DeleteApiKeyStoreWorker.Params> {
    interface Params : WorkParameters {
        val apiPortalServer: Property<String>
        val apiPortalClientId: Property<String>
        val apiPortalClientSecret: Property<String>
        val authServer: Property<String>

        val name: Property<String>
    }

    private val gradleLogger = GradleLogger.getLogger(DeleteApiKeyStoreWorker::class.qualifiedName)

    override fun execute() {
        val apiManagementClient = HttpClient(CIO) {
            Logging {
                setupGradleLogging(gradleLogger)
            }
            sapciAuth(
                clientId = parameters.apiPortalClientId.get(),
                clientSecret = parameters.apiPortalClientSecret.get(),
                authServer = parameters.authServer.get(),
            )
            defaultRequest {
                url("${parameters.apiPortalServer.get()}/apiportal/api/1.0/Management.svc/")
            }
            install(ContentNegotiation) {
                jsonIo(
                    Json {
                        useAlternativeNames = false
                    },
                )
            }
        }

        runBlocking {
            apiManagementClient.deleteCredentialsStore(parameters.name.get())
        }
    }
}
