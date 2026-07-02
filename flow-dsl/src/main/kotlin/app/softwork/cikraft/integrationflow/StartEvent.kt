package app.softwork.cikraft.integrationflow

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class StartEvent(
    val id: String,
    val name: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val extensionElements: ExtensionElements?,
    @XmlElement
    @XmlSerialName("outgoing", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val outgoing: String,
    @XmlElement
    @XmlSerialName("messageEventDefinition", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val messageEventDefinition: String? = null,
    @XmlElement
    @XmlSerialName("timerEventDefinition", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val timerEventDefinition: TimerEventDefinition? = null,

    @XmlElement
    @XmlSerialName("errorEventDefinition", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val errorEventDefinition: EndEvent.ErrorEventDefinition? = null,
) {
    @Serializable
    public data class TimerEventDefinition(
        val id: String,
        @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
        val extensionElements: ExtensionElements,
    )
}
