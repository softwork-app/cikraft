package app.softwork.cikraft.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.FormUrlEncoded
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

public fun <T : HttpClientEngineConfig> HttpClientConfig<T>.setupRuntimeAuth(
    tokenUrl: String,
    clientId: String,
    clientSecret: String,
) {
    Auth {
        bearer {
            refreshTokens {
                val oAuth2Token = client.getRuntimeToken(
                    tokenUrl = tokenUrl,
                    clientId = clientId,
                    clientSecret = clientSecret,
                ) {
                    markAsRefreshTokenRequest()
                }
                BearerTokens(oAuth2Token.accessToken, oAuth2Token.refreshToken ?: "")
            }
        }
    }
}

public suspend fun HttpClient.getRuntimeToken(
    tokenUrl: String,
    clientId: String,
    clientSecret: String,
    builder: HttpRequestBuilder.() -> Unit = {},
): OAuth2Token = post(tokenUrl) {
    parameter("grant_type", "client_credentials")
    contentType(FormUrlEncoded)
    basicAuth(
        username = clientId,
        password = clientSecret,
    )
    builder()
}.body<OAuth2Token>()

@Serializable
public data class OAuth2Token(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    val scope: String? = null,
    val expired: Boolean = false,
    val jti: String? = null,
)
