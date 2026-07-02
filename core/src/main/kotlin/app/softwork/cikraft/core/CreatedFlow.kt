package app.softwork.cikraft.core

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
public data class CreatedFlow(
    public val id: String,
    public val rawId: String,
    public val name: String,
    public val rawName: String,
    val description: String? = null,
    public val packageID: String,
    public val packageName: String,
    public val packageDescription: String? = null,
    public val sender: Sender?,
    val source: List<String> = emptyList(),
    val target: List<String> = emptyList(),
    val scripts: List<Script>,
    val injectedScripts: List<Script>,
) {
    @Serializable
    public sealed interface Sender {
        @Serializable
        public data class Https(val url: String, val role: String, val csrfProtection: Boolean) : Sender

        @Serializable
        public data class DataStore(val name: String, val pollDelay: Duration) : Sender
    }
}
