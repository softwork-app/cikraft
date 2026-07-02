package app.softwork.cikraft.api

import kotlinx.serialization.*

@Serializable
public data class Deferred(
    @SerialName("__deferred")
    val deferred: DeferredUri,
) {
    @Serializable
    public data class DeferredUri(val uri: String)
}
