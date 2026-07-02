package app.softwork.cikraft.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.Configuration
import io.ktor.serialization.kotlinx.json.*
import kotlinx.io.Source
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    useAlternativeNames = false
}

// https://api.sap.com/api/IntegrationContent/resource/Integration_Packages_Design
public fun <T : HttpClientEngineConfig> HttpClientConfig<T>.setupSapCIApiClient(
    clientId: String,
    clientSecret: String,
    apiServer: String,
    authServer: String,
) {
    sapciAuth(
        clientId = clientId,
        clientSecret = clientSecret,
        authServer = authServer,
    )
    defaultRequest {
        url("$apiServer/api/v1/")
    }
    install(ContentNegotiation) {
        sapciSerialization()
    }
    install(HttpTimeout) {
        requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
    }
}

public fun Configuration.sapciSerialization() {
    jsonIo(json)
}

public fun <T : HttpClientEngineConfig> HttpClientConfig<T>.sapciAuth(
    clientId: String,
    clientSecret: String,
    authServer: String,
) {
    Auth {
        bearer {
            refreshTokens {
                val accessToken = client.getOauthToken(
                    clientId = clientId,
                    clientSecret = clientSecret,
                    authServer = authServer,
                ) {
                    markAsRefreshTokenRequest()
                }
                BearerTokens(accessToken, null)
            }
        }
    }
}

public suspend fun HttpClient.getOauthToken(
    clientId: String,
    clientSecret: String,
    authServer: String,
    builder: suspend HttpRequestBuilder.() -> Unit = {},
): String {
    val response = get(urlString = """$authServer/oauth/token""") {
        parameter("grant_type", "client_credentials")
        basicAuth(clientId, clientSecret)
        contentType(ContentType.Application.FormUrlEncoded)
        expectSuccess = true
        builder()
    }
    return response.body<Auth>().accessToken
}

internal const val CSRF_TOKEN = "X-CSRF-Token"

public suspend fun HttpClient.getCSRFToken(): String = get {
    header(CSRF_TOKEN, "Fetch")
    expectSuccess = true
}.headers[CSRF_TOKEN]!!

public suspend fun HttpClient.getIntegrationPackages(
    builder: suspend HttpRequestBuilder.() -> Unit = {},
): List<IntegrationPackage> {
    val response = get("IntegrationPackages") {
        builder()
    }
    return response.getBodyOrThrow(
        ResponseWrapper.serializer(
            ResultsWrapper.serializer(
                IntegrationPackage.serializer(),
            ),
        ),
    ).d.results
}

public suspend fun HttpClient.getIntegrationPackage(
    id: String,
    builder: suspend HttpRequestBuilder.() -> Unit = {},
): IntegrationPackage? {
    val response = get("IntegrationPackages('$id')") {
        builder()
    }
    return response.getBodyOrNull(
        ResponseWrapper.serializer(
            IntegrationPackage.serializer(),
        ),
    )?.d
}

public suspend fun HttpClient.createIntegrationPackage(
    integrationPackage: IntegrationPackage.New,
    builder: suspend HttpRequestBuilder.() -> Unit = {},
): IntegrationPackage {
    val token = getCSRFToken()
    val response = post("IntegrationPackages") {
        header(CSRF_TOKEN, token)
        setBody(json.encodeToString(integrationPackage))
        contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
        builder()
    }
    return response.getBodyOrThrow(
        ResponseWrapper.serializer(
            IntegrationPackage.serializer(),
        ),
    ).d
}

public suspend fun HttpClient.deleteIntegrationPackage(
    id: String,
    builder: suspend HttpRequestBuilder.() -> Unit = {},
) {
    val token = getCSRFToken()
    delete("IntegrationPackages('$id')") {
        header(CSRF_TOKEN, token)
        builder()
    }
}

public suspend fun HttpClient.getAllIntegrationFlows(
    packageID: String,
): List<IntegrationFlow> {
    val response = get("IntegrationPackages('$packageID')/IntegrationDesigntimeArtifacts")
    return response.getBodyOrThrow(
        ResponseWrapper.serializer(
            ResultsWrapper.serializer(
                IntegrationFlow.serializer(),
            ),
        ),
    ).d.results
}

public suspend fun HttpClient.getIntegrationFlow(
    id: String,
    version: String = "Active",
): IntegrationFlow? {
    val response = get("IntegrationDesigntimeArtifacts(Id='$id',Version='$version')")
    return response.getBodyOrNull(
        ResponseWrapper.serializer(
            IntegrationFlow.serializer(),
        ),
    )?.d
}

public suspend fun HttpClient.getIntegrationFlowAsZip(
    id: String,
    version: String = "Active",
): Source {
    val response = get($$"IntegrationDesigntimeArtifacts(Id='$$id',Version='$$version')/$value")
    return response.body()
}

public suspend fun HttpClient.createIntegrationFlow(
    integrationFlow: IntegrationFlow.New,
): IntegrationFlow {
    val url = "IntegrationDesigntimeArtifacts"
    val token = getCSRFToken()
    val response = post(url) {
        header(CSRF_TOKEN, token)
        setBody(json.encodeToString(integrationFlow))
        contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
    }
    return response.getBodyOrThrow(
        ResponseWrapper.serializer(
            IntegrationFlow.serializer(),
        ),
    ).d
}

public suspend fun HttpClient.deleteIntegrationFlow(
    id: String,
    version: String = "Active",
) {
    val url = "IntegrationDesigntimeArtifacts(Id='$id',Version='$version')"
    val token = getCSRFToken()
    delete(url) {
        header(CSRF_TOKEN, token)
    }
}

public suspend fun HttpClient.deployIntegrationFlow(
    id: String,
    version: String = "Active",
    builder: suspend HttpRequestBuilder.() -> Unit = {},
): String? {
    val token = getCSRFToken()
    val response = post("DeployIntegrationDesigntimeArtifact") {
        parameter("Id", "'$id'")
        parameter("Version", "'$version'")

        header(CSRF_TOKEN, token)
        builder()
    }
    val body = response.bodyAsText()
    return if (response.status.isSuccess()) {
        body
    } else if (response.status == HttpStatusCode.NotFound) {
        null
    } else {
        val error = json.decodeFromString(OdataError.serializer(), body)
        throw error
    }
}

public suspend fun HttpClient.undeployIntegrationFlow(
    id: String,
    builder: suspend HttpRequestBuilder.() -> Unit = {},
) {
    val token = getCSRFToken()
    delete("IntegrationRuntimeArtifacts(Id='$id')") {
        header(CSRF_TOKEN, token)
        builder()
    }
}

public suspend fun HttpClient.getBuildAndDeployStatus(
    taskId: String,
    builder: suspend HttpRequestBuilder.() -> Unit = {},
): BuildAndDeployStatus {
    val response = get("BuildAndDeployStatus(TaskId='$taskId')") {
        builder()
    }
    return response.getBodyOrThrow(
        ResponseWrapper.serializer(
            BuildAndDeployStatus.serializer(),
        ),
    ).d
}

@Serializable
public data class ResponseWrapper<T : Any>(val d: T)

@Serializable
public data class ResultsWrapper<T : Any>(val results: List<T>)

public suspend fun <T : Any> HttpResponse.getBodyOrThrow(bodySerializer: KSerializer<T>): T {
    val body = bodyAsText()
    return if (status.isSuccess()) {
        json.decodeFromString(bodySerializer, body)
    } else {
        val error = json.decodeFromString(OdataError.serializer(), body)
        throw error
    }
}

public suspend fun <T : Any> HttpResponse.getBodyOrNull(bodySerializer: KSerializer<T>): T? {
    val body = bodyAsText()
    return if (status.isSuccess()) {
        json.decodeFromString(bodySerializer, body)
    } else if (status == HttpStatusCode.NotFound) {
        null
    } else {
        val error = json.decodeFromString(OdataError.serializer(), body)
        throw error
    }
}
