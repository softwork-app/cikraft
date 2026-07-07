package app.softwork.cikraft.gradle

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
import org.gradle.workers.WorkerExecutor
import java.net.URI
import javax.inject.Inject

@UntrackedTask(because = "Because creating a provider is a remote operation")
abstract class CreateApiRuntimeProviderTask @Inject constructor(stageName: String) : DefaultTask() {
    init {
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:Input
    abstract val apiPortalServer: Property<String>

    @get:Input
    abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class,
        "${SAP_CI_CREDENTIALS}Api$stageName",
    )

    @get:Input abstract val providerName: Property<String>

    @get:Input abstract val providerTitle: Property<String>

    @get:Input abstract val providerCredentialStoreName: Property<String>

    @get:Input
    internal abstract val httpServer: Property<String>

    @get:Input abstract val providerCredentialName: Property<String>

    @get:Input
    internal val credentialPassword: Provider<String> = providerCredentialName.flatMap {
        project.providers.gradleProperty(
            SAP_CI_CREDENTIALS_API_CREDENTIALS + it + "Password",
        )
    }

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun createApiProvider() {
        workerExecutor.processIsolation {
            this.classpath.from(workerClasspath)
        }.submit(CreateApiRuntimeProviderWorker::class) {
            this.apiPortalServer.set(this@CreateApiRuntimeProviderTask.apiPortalServer)
            this.apiPortalClientId.set(this@CreateApiRuntimeProviderTask.credentials.map { it.username })
            this.apiPortalClientSecret.set(this@CreateApiRuntimeProviderTask.credentials.map { it.password })
            this.authServer.set(this@CreateApiRuntimeProviderTask.authServer)
            this.name.set(this@CreateApiRuntimeProviderTask.providerName)
            this.title.set(this@CreateApiRuntimeProviderTask.providerTitle)
            this.runtimeHost.set(URI(this@CreateApiRuntimeProviderTask.httpServer.get()).host)
            this.credentialStoreName.set(this@CreateApiRuntimeProviderTask.providerCredentialStoreName)
            this.credentialName.set(this@CreateApiRuntimeProviderTask.providerCredentialName)
            this.credentialPassword.set(this@CreateApiRuntimeProviderTask.credentialPassword)
        }
    }
}
