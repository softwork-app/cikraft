package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.IntegrationPackage
import app.softwork.cikraft.api.createIntegrationPackage
import app.softwork.cikraft.api.deleteIntegrationPackage
import app.softwork.cikraft.api.getIntegrationPackage
import app.softwork.cikraft.api.setupSapCIApiClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.logging.Logging as GradleLogger

abstract class CreateIntegrationPackageForTransportWorker :
    WorkAction<CreateIntegrationPackageForTransportWorker.Params> {
    interface Params : WorkParameters {
        val apiServer: Property<String>
        val authServer: Property<String>
        val clientID: Property<String>
        val clientSecret: Property<String>

        val packageName: Property<String>
        val packageID: Property<String>
        val packageVersion: Property<String>
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
                if (foundPackage != null) {
                    client.deleteIntegrationPackage(parameters.packageID.get())
                }

                client.createIntegrationPackage(
                    IntegrationPackage.New(
                        id = parameters.packageID.get(),
                        name = parameters.packageName.get(),
                        shortText = parameters.packageDescription.orNull ?: "",
                        version = parameters.packageVersion.orNull,
                    ),
                )
            }
        }
    }
}
