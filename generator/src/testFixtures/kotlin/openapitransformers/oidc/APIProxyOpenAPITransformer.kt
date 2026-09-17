package openapitransformers.oidc

import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.SAPOpenAPITransformer
import io.github.hfhbd.kfx.openapi.model.OpenApi
import kotlin.String
import kotlin.collections.buildMap
import kotlin.collections.emptyList
import kotlin.collections.listOf
import kotlin.collections.mapOf

public class APIProxyOpenAPITransformer : SAPOpenAPITransformer {
  override fun convert(openApi: OpenApi, infrastructure: OpenApiInfrastructure): OpenApi {
    val paths = openApi.paths
    val apiPaths = buildMap<String, OpenApi.Path> {
      this["/proxy"] = paths["/iflow"]!!.let { it.copy(head = it.head?.copy(security = listOf(mapOf("MyIdp" to emptyList())), servers = emptyList()), post = it.post!!.copy(security = listOf(mapOf("MyIdp" to emptyList())), servers = emptyList())) }
    }
    return openApi.copy(servers = listOf(OpenApi.Server(url = "https://api.example.com/http/foo"), OpenApi.Server(url = "https://api-qs.example.com/http/foo")), paths = apiPaths, components = openApi.components.copy(securitySchemes = mapOf("MyIdp" to OpenApi.SecurityScheme.OpenIdConnect(openIdConnectUrl = "https://idp.example.com/.well-known/openid-configuration"))))
  }
}
