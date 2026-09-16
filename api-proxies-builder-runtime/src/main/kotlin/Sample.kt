import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.SAPOpenAPITransformer
import io.github.hfhbd.kfx.openapi.model.OpenApi

public class APIProxyOpenAPITransformer : SAPOpenAPITransformer {
    override fun convert(
        openApi: OpenApi,
        infrastructure: OpenApiInfrastructure,
    ): OpenApi {
        val paths = openApi.paths
        val apiPaths = buildMap {
            this["apiPath"] = paths["path"]!!.let { it.copy(post = it.post!!.copy(security = listOf(), servers = listOf())) }
            this["apiPath"] = paths["path"]!!.let { it.copy(post = it.post!!.copy(security = listOf(), servers = listOf())) }
            this["apiPath"] = paths["path"]!!.let { it.copy(post = it.post!!.copy(security = listOf(), servers = listOf())) }
        }

        return openApi.copy(servers = emptyList(), paths = apiPaths)
    }
}
