package app.softwork.cikraft.gradle

import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class TransportLockBuildService : BuildService<TransportLockBuildService.Parameters> {
    interface Parameters : BuildServiceParameters {
        val apiServer: Property<String>
        val authServer: Property<String>

        val packageName: Property<String>
        val packageID: Property<String>
        val packageVersion: Property<String>
        val packageDescription: Property<String>

        val apiSourceStage: Property<String>

        val workerClasspath: ConfigurableFileCollection
    }

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Inject
    internal abstract val providers: ProviderFactory

    init {
        val apiSourceStage = parameters.apiSourceStage.get()
        val credentials = providers.credentials(
            PasswordCredentials::class,
            SAP_CI_CREDENTIALS + apiSourceStage,
        )

        val queue = workerExecutor.classLoaderIsolation {
            classpath.from(parameters.workerClasspath)
        }
        queue.submit(CreateIntegrationPackageForTransportWorker::class) {
            apiServer.set(parameters.apiServer)
            authServer.set(parameters.authServer)
            clientID.set(credentials.map { it.username })
            clientSecret.set(credentials.map { it.password })

            packageName.set(parameters.packageName)
            packageID.set(parameters.packageID)
            packageVersion.set(parameters.packageVersion)
            packageDescription.set(parameters.packageDescription)
        }
        queue.await()
    }
}
