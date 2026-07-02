package com.example.ktor.resources

import com.example.JsonFactory
import com.example.core.Fault
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.serialization

public fun <T : HttpClientEngineConfig> HttpClientConfig<T>.setupBazAClient() {
  install(Resources)
  install(HttpCookies)
  install(ContentNegotiation) {
    serialization(Json.withParameter("charset", "utf-8"), JsonFactory)
    serialization(Json.withParameter("charset", "utf-8"), JsonFactory)
    serialization(Json, Fault.ErrorJsonFactory)
  }
}
