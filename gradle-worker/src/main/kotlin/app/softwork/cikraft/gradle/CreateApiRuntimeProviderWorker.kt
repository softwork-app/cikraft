package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.proxy.Provider
import app.softwork.cikraft.api.proxy.createApiProvider
import app.softwork.cikraft.api.proxy.getApiProvider
import app.softwork.cikraft.api.sapciAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.jsonIo
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.logging.Logging as GradleLogger

public abstract class CreateApiRuntimeProviderWorker : WorkAction<CreateApiRuntimeProviderWorker.Params> {
    public interface Params : WorkParameters {
        public val apiPortalServer: Property<String>
        public val apiPortalClientId: Property<String>
        public val apiPortalClientSecret: Property<String>
        public val authServer: Property<String>

        public val name: Property<String>
        public val title: Property<String>
        public val runtimeHost: Property<String>
        public val credentialStoreName: Property<String>
        public val credentialName: Property<String>
        public val credentialPassword: Property<String>
    }

    private val gradleLogger = GradleLogger.getLogger(CreateApiRuntimeProviderWorker::class.qualifiedName)

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
            if (apiManagementClient.getApiProvider(parameters.name.get()) == null) {
                apiManagementClient.createApiProvider(
                    Provider.New(
                        destType = Provider.DestType.CPIRuntime,
                        host = parameters.runtimeHost.get(),
                        name = parameters.name.get(),
                        port = 443,
                        useSSL = true,
                        isOnPremise = false,
                        rtAuth = null,
                        userName = "",
                        trustAll = false,
                        title = parameters.title.get(),
                        keystoreLocation = parameters.credentialName.get(),
                        keystorePassword = parameters.credentialPassword.get(),
                        authType = Provider.AuthType.CLIENTCERTIFICATEAUTHENTICATION,
                        password = "",
                        sslInfo = Provider.SslInfo(
                            enabled = true,
                            clientAuthEnabled = true,
                            keyStore = parameters.credentialStoreName.get(),
                            keyAlias = parameters.credentialName.get(),
                            ignoreValidationErrors = false,
                            ciphers = "",
                            protocols = "",
                        ),
                    ),
                )
            }
        }
    }
}
