package app.softwork.cikraft.integrationflow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("projectDescription")
public data class ProjectDescription(
    @XmlElement
    val name: String,
    val comment: Unit = Unit,
    val projects: Unit = Unit,
    @XmlSerialName("buildSpec")
    val buildSpec: BuildSpec,
    @XmlSerialName("natures")
    val natures: Natures,
) {
    @Serializable
    public data class BuildSpec(
        @XmlSerialName("buildCommand")
        val buildCommand: BuildCommand,
    ) {
        @Serializable
        public data class BuildCommand(
            @XmlElement
            val name: String,
            val arguments: Unit = Unit,
        )
    }

    @Serializable
    public data class Natures(public val natures: List<Nature>)

    @Serializable
    @JvmInline
    @XmlSerialName("nature")
    public value class Nature(public val value: String)
}
