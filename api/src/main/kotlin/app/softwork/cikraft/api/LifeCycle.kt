package app.softwork.cikraft.api

import kotlinx.serialization.*

@Serializable
public data class LifeCycle(
    @SerialName("__metadata") val metadata: Metadata,
    @SerialName("changed_at") val changedAt: String? = null,
    @SerialName("changed_by") val changedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
)
