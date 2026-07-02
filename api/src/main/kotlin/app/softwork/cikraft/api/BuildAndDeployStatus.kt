package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class BuildAndDeployStatus(
    @SerialName("__metadata") val metadata: Metadata? = null,
    @SerialName("TaskId")
    val taskId: String,
    @SerialName("Status")
    val status: Status,
) {
    @Serializable
    public enum class Status {
        @SerialName("DEPLOYING")
        Deploying,

        @SerialName("FAIL")
        Fail,

        @SerialName("SUCCESS")
        Success,
    }
}
