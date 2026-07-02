package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class IntegrationFlow(
    @SerialName("__metadata") val metadata: Metadata? = null,
    @SerialName("Id") val id: String,
    @SerialName("Version") val version: String,
    @SerialName("PackageId") val packageId: String,
    @SerialName("Name") val name: String,
    @SerialName("Description") val description: String,
    @SerialName("Sender") val sender: String? = null,
    @SerialName("Receiver") val receiver: String? = null,
    @SerialName("CreatedBy") val createdBy: String? = null,
    @SerialName("CreatedAt") val createdAt: Long? = null,
    @SerialName("ModifiedBy") val modifiedBy: String? = null,
    @SerialName("ModifiedAt") val modifiedAt: Long? = null,
    @SerialName("ArtifactContent") val artifactContent: String? = null,
    @SerialName("Configurations") val configurations: Deferred? = null,
    @SerialName("Resources") val resources: Deferred? = null,
    @SerialName("DesignGuidelineExecutionResults") val designGuidelineExecutionResults: Deferred? = null,
) {
    @Serializable
    public data class New(
        @SerialName("Name") val name: String,
        @SerialName("Id") val id: String,
        @SerialName("PackageId") val packageId: String,
        @SerialName("ArtifactContent") val artifactContentAsBase64: String?,
    )
}
