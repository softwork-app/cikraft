package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Auth(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: TokenType,
    @SerialName("expires_in")
    val expiresIn: Int,
    val scope: String,
    val jti: String,
) {
    @Serializable
    public enum class TokenType {
        @SerialName("bearer")
        Bearer,
    }
}
