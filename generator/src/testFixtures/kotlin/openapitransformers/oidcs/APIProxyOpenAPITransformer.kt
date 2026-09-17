package openapitransformers.oidcs

import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.SAPOpenAPITransformer
import io.github.hfhbd.kfx.openapi.model.OpenApi
import io.github.hfhbd.kfx.openapi.model.OpenApi.SecurityScheme.Http.Scheme.Basic
import kotlin.String
import kotlin.collections.buildMap
import kotlin.collections.emptyList
import kotlin.collections.listOf
import kotlin.collections.mapOf

public class APIProxyOpenAPITransformer : SAPOpenAPITransformer {
  override fun convert(openApi: OpenApi, infrastructure: OpenApiInfrastructure): OpenApi {
    val paths = openApi.paths
    val apiPaths = buildMap<String, OpenApi.Path> {
      this["/proxy"] = paths["/iflow"]!!.let { it.copy(head = it.head?.copy(security = listOf(mapOf("MyIdp" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.example.com/http"), OpenApi.Server(url = "https://api-qs.example.com/http"))), post = it.post!!.copy(security = listOf(mapOf("MyIdp" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.example.com/http"), OpenApi.Server(url = "https://api-qs.example.com/http")))) }
      this["/bar"] = paths["/iflow"]!!.let { it.copy(head = it.head?.copy(security = listOf(mapOf("MyIdp" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.example.com/http"), OpenApi.Server(url = "https://api-qs.example.com/http"))), post = it.post!!.copy(security = listOf(mapOf("MyIdp" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.example.com/http"), OpenApi.Server(url = "https://api-qs.example.com/http")))) }
      this["/proxy2"] = paths["/iflow2"]!!.let { it.copy(head = it.head?.copy(security = listOf(mapOf("MyIdp2" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.bar.com/http"), OpenApi.Server(url = "https://api-qs.bar.com/http"))), post = it.post!!.copy(security = listOf(mapOf("MyIdp2" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.bar.com/http"), OpenApi.Server(url = "https://api-qs.bar.com/http")))) }
      this["/basic"] = paths["/iflow"]!!.let { it.copy(head = it.head?.copy(security = listOf(mapOf("basic" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.example.com/http"), OpenApi.Server(url = "https://api-qs.example.com/http"))), post = it.post!!.copy(security = listOf(mapOf("basic" to emptyList())), servers = listOf(OpenApi.Server(url = "https://api.example.com/http"), OpenApi.Server(url = "https://api-qs.example.com/http")))) }
    }
    return openApi.copy(servers = listOf(OpenApi.Server(url = "https://api.example.com/http/foo"), OpenApi.Server(url = "https://api-qs.example.com/http/foo"), OpenApi.Server(url = "https://api.bar.com/http/foo"), OpenApi.Server(url = "https://api-qs.bar.com/http/foo")), paths = apiPaths, components = openApi.components.copy(securitySchemes = mapOf("MyIdp" to OpenApi.SecurityScheme.OpenIdConnect(openIdConnectUrl = "https://idp.example.com/.well-known/openid-configuration"), "MyIdp2" to OpenApi.SecurityScheme.OpenIdConnect(openIdConnectUrl = "https://idp.example.com/.well-known/openid-configuration"), "basic" to OpenApi.SecurityScheme.Http(scheme = Basic))))
  }
}
