package app.softwork.cikraft.gradle

import apiProxies
import app.softwork.cikraft.api.*
import app.softwork.cikraft.api.proxy.*
import app.softwork.cikraft.proxy.ApiProxyBuilder
import app.softwork.cikraft.proxy.ApiProxyTransport
import app.softwork.cikraft.proxy.ApiState
import app.softwork.cikraft.proxy.ServiceCode
import app.softwork.cikraft.proxy.builder.ApiProxiesBuilder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.HttpTimeoutConfig.Companion.INFINITE_TIMEOUT_MS
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.jsonIo
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.gradle.api.provider.Property
import org.gradle.workers.*
import app.softwork.cikraft.proxy.apiProxy as apiProxyOrigin
import org.gradle.api.logging.Logging as GradleLogger

public abstract class CreateApiProxiesWorker : WorkAction<CreateApiProxiesWorker.Params> {
    public interface Params : WorkParameters {
        public val url: Property<String>
        public val httpSuffix: Property<String>
        public val apiPortalServer: Property<String>
        public val apiPortalClientId: Property<String>
        public val apiPortalClientSecret: Property<String>
        public val virtualHost: Property<String>
        public val authServer: Property<String>
    }

    private val gradleLogger = GradleLogger.getLogger(CreateApiProxiesWorker::class.qualifiedName)

    override fun execute() {
        val all = mutableListOf<ApiProxyTransport>()

        object : ApiProxiesBuilder {
            override fun apiProxy(
                name: String,
                title: String,
                description: String?,
                isVersioned: Boolean,
                serviceCode: ServiceCode,
                apiState: ApiState,
                builder: ApiProxyBuilder.() -> Unit,
            ) {
                val created = apiProxyOrigin(
                    name = name,
                    title = title,
                    description = description,
                    isVersioned = isVersioned,
                    serviceCode = serviceCode,
                    apiState = apiState,
                    builder = builder,
                )
                all.add(created)
            }
        }.apiProxies(parameters.url.get(), parameters.httpSuffix.get())

        runBlocking {
            val transportClient = HttpClient(CIO) {
                Logging {
                    setupGradleLogging(gradleLogger)
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = INFINITE_TIMEOUT_MS
                }

                sapciAuth(
                    clientId = parameters.apiPortalClientId.get(),
                    clientSecret = parameters.apiPortalClientSecret.get(),
                    authServer = parameters.authServer.get(),
                )
                defaultRequest {
                    url("${parameters.apiPortalServer.get()}/apiportal/api/1.0/Transport.svc/")
                }
                install(ContentNegotiation) {
                    jsonIo(
                        Json {
                            useAlternativeNames = false
                        },
                    )
                }
                expectSuccess = true
            }
            for (transport in all) {
                transportClient.transportProxy(
                    virtualHost = parameters.virtualHost.get(),
                    apiProxyAsBase64 = transport.toBase64(),
                )
            }
        }
    }
}

public abstract class DeleteApiProxiesWorker : WorkAction<DeleteApiProxiesWorker.Params> {
    public interface Params : WorkParameters {
        public val url: Property<String>
        public val httpSuffix: Property<String>
        public val apiPortalServer: Property<String>
        public val apiPortalClientId: Property<String>
        public val apiPortalClientSecret: Property<String>
        public val authServer: Property<String>
    }

    private val gradleLogger = GradleLogger.getLogger(DeleteApiProxiesWorker::class.qualifiedName)

    override fun execute() {
        val all = mutableListOf<ApiProxyTransport>()

        object : ApiProxiesBuilder {
            override fun apiProxy(
                name: String,
                title: String,
                description: String?,
                isVersioned: Boolean,
                serviceCode: ServiceCode,
                apiState: ApiState,
                builder: ApiProxyBuilder.() -> Unit,
            ) {
                val created = apiProxyOrigin(
                    name = name,
                    title = title,
                    description = description,
                    isVersioned = isVersioned,
                    serviceCode = serviceCode,
                    apiState = apiState,
                    builder = builder,
                )
                all.add(created)
            }
        }.apiProxies(parameters.url.get(), parameters.httpSuffix.get())

        val apiManagementClient = HttpClient(CIO) {
            Logging {
                setupGradleLogging(gradleLogger)
            }

            sapciAuth(
                clientId = parameters.apiPortalClientId.get(),
                clientSecret = parameters.apiPortalClientSecret.get(),
                authServer = parameters.authServer.get(),
            )
            install(ContentNegotiation) {
                jsonIo(
                    Json {
                        useAlternativeNames = false
                    },
                )
            }
            defaultRequest {
                url("${parameters.apiPortalServer.get()}/apiportal/api/1.0/Management.svc/")
            }
        }
        runBlocking {
            for (transport in all) {
                apiManagementClient.deleteApiProxy(name = transport.name)
            }
        }
    }
}
