package app.softwork.cikraft.core

import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
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
    public val type: Type?,
    val source: List<String> = emptyList(),
    val target: List<String> = emptyList(),
    val scripts: List<Script>,
    val injectedScripts: List<Script>,
    val exceptionHandler: ExceptionHandler? = null,
) {
    @Serializable
    public sealed interface Type {
        @Serializable
        public data class Https(val url: String, val role: String, val csrfProtection: Boolean) : Type

        @Serializable
        public data class DataStore(val name: String, val pollDelay: Duration) : Type

        @Serializable
        public data class Scheduler(
            @Language("CronExp")
            val quartzCron: String,
        ) : Type
    }

    @Serializable
    public data class ExceptionHandler(val scripts: List<Script>, val injectedScripts: List<Script>)
}
