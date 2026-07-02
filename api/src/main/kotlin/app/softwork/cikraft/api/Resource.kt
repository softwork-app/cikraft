package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Resource(
    @SerialName("Name") val name: String,
    @SerialName("ResourceType") val resourceType: String,
    @SerialName("ReferencedResourceType") val referencedResourceType: String,
    @SerialName("ResourceContent") val resourceContent: String,
)
