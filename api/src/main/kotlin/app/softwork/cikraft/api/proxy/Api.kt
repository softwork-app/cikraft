package app.softwork.cikraft.api.proxy

import app.softwork.cikraft.api.ResponseWrapper
import app.softwork.cikraft.api.ResultsWrapper
import app.softwork.cikraft.api.getBodyOrNull
import app.softwork.cikraft.api.getBodyOrThrow
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.uuid.*

public suspend fun HttpClient.getAllApiProviders(): List<Provider> =
    get("APIProviders").getBodyOrThrow(
        ResponseWrapper.serializer(
            ResultsWrapper.serializer(Provider.serializer()),
        ),
    ).d.results

public suspend fun HttpClient.getApiProvider(name: String): Provider? =
    get("APIProviders('$name')").getBodyOrNull(
        ResponseWrapper.serializer(
            Provider.serializer(),
        ),
    )?.d

public suspend fun HttpClient.createApiProvider(
    newProvider: Provider.New,
): Provider =
    post("APIProviders") {
        setBody(newProvider)
        contentType(ContentType.Application.Json)
    }.getBodyOrThrow(
        ResponseWrapper.serializer(
            Provider.serializer(),
        ),
    ).d

public suspend fun HttpClient.deleteApiProvider(name: String) {
    delete("APIProviders('$name')") {
        expectSuccess = true
    }
}

public suspend fun HttpClient.createApiProxy(
    new: ApiProxy.New,
): ApiProxy = post("APIProxies") {
    setBody(new)
    contentType(ContentType.Application.Json)
}.getBodyOrThrow(
    ResponseWrapper.serializer(ApiProxy.serializer()),
).d

public suspend fun HttpClient.getApiProxies(): List<ApiProxy> = get("APIProxies").getBodyOrThrow(
    ResponseWrapper.serializer(ResultsWrapper.serializer(ApiProxy.serializer())),
).d.results

public suspend fun HttpClient.getApiProxy(name: String): ApiProxy? = get("APIProxies('$name')").getBodyOrNull(
    ResponseWrapper.serializer(ApiProxy.serializer()),
)?.d

public suspend fun HttpClient.deleteApiProxy(name: String) {
    delete("APIProxies('$name')") {
        expectSuccess = true
    }
}

public suspend fun HttpClient.getPolicies(apiProxy: String): List<Policy> =
    get("APIProxies('$apiProxy')/policies").getBodyOrThrow(
        ResponseWrapper.serializer(ResultsWrapper.serializer(Policy.serializer())),
    ).d.results

public suspend fun HttpClient.updatePolicy(
    policy: Policy.Update,
): List<Policy> = put("Policies('${policy.id}')") {
    setBody(policy)
    contentType(ContentType.Application.Json)
}.getBodyOrThrow(
    ResponseWrapper.serializer(
        ResultsWrapper.serializer(Policy.serializer()),
    ),
).d.results

public suspend fun HttpClient.createCredentials(
    credentials: Credentials.New,
): Credentials.Created = post("CertificateStores") {
    setBody(credentials)
    contentType(ContentType.Application.Json)
}.getBodyOrThrow(
    ResponseWrapper.serializer(
        Credentials.Created.serializer(),
    ),
).d

public suspend fun HttpClient.getCredentials(
    name: String,
    storeName: String,
): Credentials? = get("Certificates(name='$name',storeName='$storeName')") {
}.getBodyOrNull(
    ResponseWrapper.serializer(
        Credentials.serializer(),
    ),
)?.d

public suspend fun HttpClient.deleteCredentials(
    name: String,
    storeName: String,
) {
    delete("Certificates(name='$name',storeName='$storeName')") {
        expectSuccess = true
    }
}

public suspend fun HttpClient.deleteCredentialsStore(
    storeName: String,
) {
    delete("CertificateStores('$storeName')") {
        expectSuccess = true
    }
}

public suspend fun HttpClient.getAllCertificatesFromCredentialsStore(
    storeName: String,
): List<Credentials> = get("CertificateStores('$storeName')/certificates") {
}.getBodyOrThrow(
    ResponseWrapper.serializer(
        ResultsWrapper.serializer(
            Credentials.serializer(),
        ),
    ),
).d.results
