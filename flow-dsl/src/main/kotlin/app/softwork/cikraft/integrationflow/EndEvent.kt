package app.softwork.cikraft.integrationflow

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("endEvent", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
public data class EndEvent(
    val id: String,
    val name: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val extensionElements: ExtensionElements?,
    @XmlElement
    val incoming: String,
    @XmlElement val messageEventDefinition: String? = "",
    @XmlSerialName("errorEventDefinition", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    @XmlElement val errorEventDefinition: ErrorEventDefinition? = null,
) {
    @Serializable
    public data class ErrorEventDefinition(
        @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
        val extensionElements: ExtensionElements,
    )
}
