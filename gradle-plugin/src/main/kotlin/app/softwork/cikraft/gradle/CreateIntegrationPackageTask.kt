package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.IntegrationPackage
import app.softwork.cikraft.api.createIntegrationPackage
import app.softwork.cikraft.api.getIntegrationPackage
import app.softwork.cikraft.api.setupSapCIApiClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
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
import org.gradle.api.logging.Logging as GradleLogger

@UntrackedTask(because = "Because creating a provider is a remote operation")
abstract class CreateIntegrationPackageTask @Inject constructor(
    @get:Input
    private val stageName: String,
) : DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val packageID: Property<String>

    @get:Input
    abstract val packageDescription: Property<String>

    @get:Input
    internal abstract val apiServer: Property<String>

    @get:Input
    internal abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class,
        SAP_CI_CREDENTIALS + stageName,
    )

    init {
        group = "cikraft"
    }

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun createIntegrationPackage() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(CreateIntegrationPackageWorker::class) {
            apiServer.set(this@CreateIntegrationPackageTask.apiServer)
            authServer.set(this@CreateIntegrationPackageTask.authServer)
            clientID.set(this@CreateIntegrationPackageTask.credentials.map { it.username })
            clientSecret.set(this@CreateIntegrationPackageTask.credentials.map { it.password })

            packageName.set(this@CreateIntegrationPackageTask.packageName)
            packageID.set(this@CreateIntegrationPackageTask.packageID)
            packageDescription.set(this@CreateIntegrationPackageTask.packageDescription)
        }
    }
}

abstract class CreateIntegrationPackageWorker : WorkAction<CreateIntegrationPackageWorker.Params> {
    interface Params : WorkParameters {
        val apiServer: Property<String>
        val authServer: Property<String>
        val clientID: Property<String>
        val clientSecret: Property<String>

        val packageName: Property<String>
        val packageID: Property<String>
        val packageDescription: Property<String>
    }

    override fun execute() {
        HttpClient(CIO) {
            setupSapCIApiClient(
                apiServer = parameters.apiServer.get(),
                authServer = parameters.authServer.get(),
                clientId = parameters.clientID.get(),
                clientSecret = parameters.clientSecret.get(),
            )
            Logging {
                setupGradleLogging(GradleLogger.getLogger(CreateIntegrationPackageWorker::class.java))
            }
        }.use { client ->
            runBlocking {
                val foundPackage = client.getIntegrationPackage(parameters.packageID.get())
                if (foundPackage == null) {
                    client.createIntegrationPackage(
                        IntegrationPackage.New(
                            parameters.packageID.get(),
                            parameters.packageName.get(),
                            parameters.packageDescription.orNull ?: "",
                        ),
                    )
                }
            }
        }
    }
}
