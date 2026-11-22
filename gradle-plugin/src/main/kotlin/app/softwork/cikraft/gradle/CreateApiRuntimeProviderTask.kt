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
import org.gradle.workers.WorkerExecutor
import java.net.URI
import javax.inject.Inject

@UntrackedTask(because = "Because creating a provider is a remote operation")
public abstract class CreateApiRuntimeProviderTask @Inject constructor(stageName: String) : DefaultTask() {
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
    public abstract val providerName: Property<String>

    @get:Input
    public abstract val providerTitle: Property<String>

    @get:Input
    public abstract val providerCredentialStoreName: Property<String>

    @get:Input
    internal abstract val httpServer: Property<String>

    @get:Input
    public abstract val providerCredentialName: Property<String>

    @get:Input
    internal val credentialPassword: Provider<String> = providerCredentialName.flatMap {
        project.providers.gradleProperty(
            SAP_CI_CREDENTIALS_API_CREDENTIALS + it + "Password",
        )
    }

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun createApiProvider() {
        workerExecutor.processIsolation {
            it.classpath.from(workerClasspath)
        }.submit(CreateApiRuntimeProviderWorker::class.java) {
            it.apiPortalServer.set(apiPortalServer)
            it.apiPortalClientId.set(credentials.map { it.username })
            it.apiPortalClientSecret.set(credentials.map { it.password })
            it.authServer.set(authServer)
            it.name.set(providerName)
            it.title.set(providerTitle)
            it.runtimeHost.set(URI(httpServer.get()).host)
            it.credentialStoreName.set(providerCredentialStoreName)
            it.credentialName.set(providerCredentialName)
            it.credentialPassword.set(credentialPassword)
        }
    }
}
