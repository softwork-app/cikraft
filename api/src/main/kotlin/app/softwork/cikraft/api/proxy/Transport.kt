package app.softwork.cikraft.api.proxy

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

public suspend fun HttpClient.transportProxy(
    virtualHost: String,
    apiProxyAsBase64: String,
) {
    post("APIProxies") {
        parameter("virtualhost", virtualHost)
        contentType(ContentType.Application.OctetStream)
        setBody(apiProxyAsBase64)
        expectSuccess = true
    }
}
