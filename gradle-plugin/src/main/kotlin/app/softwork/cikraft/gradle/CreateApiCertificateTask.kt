package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.*
import app.softwork.cikraft.api.proxy.Credentials
import app.softwork.cikraft.api.proxy.Credentials.Certificate.Format.*
import app.softwork.cikraft.api.proxy.Credentials.New.StoreType.*
import app.softwork.cikraft.api.proxy.createCredentials
import app.softwork.cikraft.api.proxy.deleteCredentials
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
import org.gradle.workers.*
import javax.inject.*
import kotlin.io.encoding.*
import org.gradle.api.logging.Logging as GradleLogger

@UntrackedTask(because = "Creating an API certificate is a remote operation")
public abstract class CreateApiCertificateTask @Inject constructor(stageName: String) : DefaultTask() {
    init {
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input
    public abstract val apiPortalServer: Property<String>

    @get:Input
    public abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class.java,
        "${SAP_CI_CREDENTIALS}Api$stageName",
    )

    @get:Input
    public abstract val apiCertificateName: Property<String>

    @get:Input
    internal val apiCertificatePassword: Provider<String> = apiCertificateName.flatMap {
        project.providers.gradleProperty(
            SAP_CI_CREDENTIALS_API_CREDENTIALS + it + "Password",
        )
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val apiCertificateP12File: RegularFileProperty

    @get:Input
    public abstract val apiCertificateStoreName: Property<String>

    @get:Input
    @get:Optional
    public abstract val apiCertificateDescription: Property<String>

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun createApiCertificate() {
        workerExecutor.processIsolation {
            it.classpath.from(workerClasspath)
        }.submit(CreateApiCertificateWorker::class.java) {
            it.apiPortalServer.set(apiPortalServer)
            it.apiPortalClientId.set(credentials.map { it.username })
            it.apiPortalClientSecret.set(credentials.map { it.password })
            it.authServer.set(authServer)
            it.storeName.set(apiCertificateStoreName)
            it.name.set(apiCertificateName)
            it.p12File.set(apiCertificateP12File)
            it.password.set(apiCertificatePassword)
            it.description.set(apiCertificateDescription)
        }
    }
}

internal abstract class CreateApiCertificateWorker : WorkAction<CreateApiCertificateWorker.Params> {
    interface Params : WorkParameters {
        val apiPortalServer: Property<String>
        val apiPortalClientId: Property<String>
        val apiPortalClientSecret: Property<String>
        val authServer: Property<String>
        val storeName: Property<String>
        val name: Property<String>
        val p12File: RegularFileProperty
        val password: Property<String>
        val description: Property<String>
    }

    private val gradleLogger = GradleLogger.getLogger(CreateApiCertificateWorker::class.qualifiedName)

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
            apiManagementClient.createCredentials(
                Credentials.New(
                    storeName = parameters.storeName.get(),
                    storeType = KeyStore,
                    certificates = listOf(
                        Credentials.Certificate(
                            name = parameters.name.get(),
                            format = PKCS12,
                            base64Content = Base64.encode(parameters.p12File.get().asFile.readBytes()),
                            password = parameters.password.get(),
                            description = parameters.description.orNull,
                        ),
                    ),
                ),
            )
        }
    }
}

@UntrackedTask(because = "Deleting an API certificate is a remote operation")
public abstract class DeleteApiCertificateTask @Inject constructor(
    stageName: String,
    @get:Input public val credentialName: String,
) : DefaultTask() {
    init {
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input
    public abstract val apiPortalServer: Property<String>

    @get:Input
    public abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class.java,
        "${SAP_CI_CREDENTIALS}Api$stageName",
    )

    @get:Input public abstract val storeName: Property<String>

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun deleteApiCertificate() {
        workerExecutor.processIsolation {
            it.classpath.from(workerClasspath)
        }.submit(DeleteApiCertificateWorker::class.java) {
            it.apiPortalServer.set(apiPortalServer)
            it.apiPortalClientId.set(credentials.map { it.username })
            it.apiPortalClientSecret.set(credentials.map { it.password })
            it.authServer.set(authServer)
            it.storeName.set(storeName)
            it.name.set(credentialName)
        }
    }
}

internal abstract class DeleteApiCertificateWorker : WorkAction<DeleteApiCertificateWorker.Params> {
    interface Params : WorkParameters {
        val apiPortalServer: Property<String>
        val apiPortalClientId: Property<String>
        val apiPortalClientSecret: Property<String>
        val authServer: Property<String>
        val storeName: Property<String>
        val name: Property<String>
    }

    private val gradleLogger = GradleLogger.getLogger(DeleteApiCertificateWorker::class.qualifiedName)

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
            apiManagementClient.deleteCredentials(parameters.name.get(), parameters.storeName.get())
        }
    }
}
