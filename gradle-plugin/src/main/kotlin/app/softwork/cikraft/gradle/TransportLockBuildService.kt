package app.softwork.cikraft.gradle

import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

public abstract class TransportLockBuildService : BuildService<TransportLockBuildService.Parameters> {
    public interface Parameters : BuildServiceParameters {
        public val apiServer: Property<String>
        public val authServer: Property<String>

        public val packageName: Property<String>
        public val packageID: Property<String>
        public val packageVersion: Property<String>
        public val packageDescription: Property<String>

        public val apiSourceStage: Property<String>

        public val workerClasspath: ConfigurableFileCollection
    }

    @get:Inject
    protected abstract val providers: ProviderFactory

    private var initialized = false

    @Synchronized
    internal fun initialize(workerExecutor: WorkerExecutor) {
        if (initialized) return

        val apiSourceStage = parameters.apiSourceStage.get()
        val credentials = providers.credentials(
            PasswordCredentials::class.java,
            SAP_CI_CREDENTIALS + apiSourceStage,
        )

        val queue = workerExecutor.classLoaderIsolation {
            it.classpath.from(parameters.workerClasspath)
        }
        queue.submit(CreateIntegrationPackageForTransportWorker::class.java) {
            it.apiServer.set(parameters.apiServer)
            it.authServer.set(parameters.authServer)
            it.clientID.set(credentials.map { it.username })
            it.clientSecret.set(credentials.map { it.password })

            it.packageName.set(parameters.packageName)
            it.packageID.set(parameters.packageID)
            it.packageVersion.set(parameters.packageVersion)
            it.packageDescription.set(parameters.packageDescription)
        }
        queue.await()
        initialized = true
    }
}
