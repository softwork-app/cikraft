package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class IntegrationPackage(
    @SerialName("__metadata") val metadata: Metadata? = null,
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String,
    @SerialName("ResourceId") val resourceId: String? = null,
    @SerialName("Description") val description: String? = null,
    @SerialName("ShortText") val shortText: String,
    @SerialName("Version") val version: String? = null,
    @SerialName("Vendor") val vendor: String? = null,
    @SerialName("PartnerContent") val partnerContent: Boolean? = null,
    @SerialName("UpdateAvailable") val updateAvailable: Boolean? = null,
    @SerialName("Mode") val mode: String? = null,
    @SerialName("SupportedPlatform") val supportedPlatform: String? = null,
    @SerialName("CreatedBy") val createdBy: String? = null,
    @SerialName("CreationDate") val creationDate: String? = null,
    @SerialName("ModifiedBy") val modifiedBy: String? = null,
    @SerialName("ModifiedDate") val modifiedDate: String? = null,
    @SerialName("Products") val products: String? = null,
    @SerialName("Keywords") val keywords: String? = null,
    @SerialName("Countries") val countries: String? = null,
    @SerialName("Industries") val industries: String? = null,
    @SerialName("LineOfBusiness") val lineOfBusiness: String? = null,
    @SerialName("PackageContent") val packageContent: String? = null,
    @SerialName("IntegrationDesigntimeArtifacts") val integrationDesigntimeArtifacts: Deferred? = null,
    @SerialName("ValueMappingDesigntimeArtifacts") val valueMappingDesigntimeArtifacts: Deferred? = null,
    @SerialName(
        "MessageMappingDesigntimeArtifacts",
    ) val messageMappingDesigntimeArtifacts: Deferred? = null,
    @SerialName(
        "ScriptCollectionDesigntimeArtifacts",
    ) val scriptCollectionDesigntimeArtifacts: Deferred? = null,
    @SerialName("CustomTags") val customTags: Deferred? = null,
) {
    @Serializable
    public data class New(
        @SerialName("Id") val id: String,
        @SerialName("Name") val name: String,
        @SerialName("ShortText") val shortText: String,
        @SerialName("Description") val description: String? = null,
        @SerialName("Version") val version: String? = null,
        @SerialName("SupportedPlatform") val supportedPlatform: Platform? = null,
        @SerialName("Products") val products: String? = null,
        @SerialName("Keywords") val keywords: String? = null,
        @SerialName("Countries") val countries: String? = null,
        @SerialName("Industries") val industries: String? = null,
        @SerialName("LineOfBusiness") val lineOfBusiness: String? = null,
    )
}
