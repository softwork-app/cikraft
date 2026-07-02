package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class Platform {
    @SerialName("SAP Cloud Integration")
    CloudIntegration,

    @SerialName("SAP Process Orchestration")
    ProcessOrchestration,

    @SerialName("SuccessFactors Integration Center")
    SuccessFactors,
}
