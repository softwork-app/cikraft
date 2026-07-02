package app.softwork.cikraft.proxy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("steps")
public data class Steps(val steps: List<Step>) {
    @Serializable
    @SerialName("step")
    public data class Step(
        @SerialName("policy_name")
        @XmlElement
        val policyName: String,
        @XmlElement
        val condition: String? = null,
        @XmlElement
        val sequence: Int,
    )
}
