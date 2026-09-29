import app.softwork.cikraft.core.OpenApiInfrastructure
import io.github.hfhbd.kfx.openapi.model.OpenApi
import io.github.hfhbd.kfx.openapi.model.json
import kotlin.test.Test
import kotlin.test.assertEquals

class APIProxyOpenAPITest {
    @Test
    fun a() {
        val openapi = json.decodeFromString(
            OpenApi.serializer(),
            APIProxyOpenAPITest::class.java.getResource("/a.json")!!.readText()
        )
        val proxyOpenApi = openapitransformers.empty.APIProxyOpenAPITransformer().convert(
            openapi,
            OpenApiInfrastructure(
                apis = emptyList(),
                name = "",
                description = "",
                version = "",
                tags = emptyMap(),
                servers = emptyMap(),
            )
        )
        assertEquals(
            json.decodeFromString(
                OpenApi.serializer(),
                APIProxyOpenAPITest::class.java.getResource("/a-proxy.json")!!.readText()
            ),
            proxyOpenApi,
        )
    }

    @Test
    fun wildcard() {
        val openapi = json.decodeFromString(
            OpenApi.serializer(),
            APIProxyOpenAPITest::class.java.getResource("/wildcard.json")!!.readText()
        )
        val proxyOpenApi = openapitransformers.oidc.APIProxyOpenAPITransformer().convert(
            openapi,
            OpenApiInfrastructure(
                apis = emptyList(),
                name = "",
                description = "",
                version = "",
                tags = emptyMap(),
                servers = emptyMap(),
            )
        )
        assertEquals(
            json.decodeFromString(
                OpenApi.serializer(),
                APIProxyOpenAPITest::class.java.getResource("/wildcard-proxy.json")!!.readText()
            ),
            proxyOpenApi,
        )
    }
}
