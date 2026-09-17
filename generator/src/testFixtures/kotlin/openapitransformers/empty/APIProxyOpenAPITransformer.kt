package openapitransformers.empty

import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.SAPOpenAPITransformer
import io.github.hfhbd.kfx.openapi.model.OpenApi
import kotlin.String
import kotlin.collections.buildMap
import kotlin.collections.emptyMap
import kotlin.collections.listOf

public class APIProxyOpenAPITransformer : SAPOpenAPITransformer {
  override fun convert(openApi: OpenApi, infrastructure: OpenApiInfrastructure): OpenApi {
    val paths = openApi.paths
    val apiPaths = buildMap<String, OpenApi.Path> {
    }
    return openApi.copy(servers = listOf(), paths = apiPaths, components = openApi.components.copy(securitySchemes = emptyMap()))
  }
}
