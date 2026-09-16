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
                APIProxyOpenAPITest::class.java.getResource("/proxy-a.json")!!.readText()
            ),
            proxyOpenApi,
        )
    }
}
