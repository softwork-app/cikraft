package app.softwork.cikraft.api.proxy

import app.softwork.cikraft.api.Deferred
import app.softwork.cikraft.api.Metadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.intellij.lang.annotations.Language
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@ExperimentalUuidApi
public data class Policy(
    @SerialName("__metadata")
    val metadata: Metadata? = null,
    val id: Uuid,
    @SerialName("life_cycle")
    val lifeCycle: Provider.Lifecycle? = null,
    val name: String,
    @SerialName("policy_content")
    @Language("xml")
    val policyContent: String,
    val type: String,
    val apiProxy: Deferred,
) {
    @Serializable
    @ExperimentalUuidApi
    public data class Update(
        val id: Uuid,
        val name: String,
        @SerialName("policy_content")
        val policyContent: String,
        val type: String,
    )
}
