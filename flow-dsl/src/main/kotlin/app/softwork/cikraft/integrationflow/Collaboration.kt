package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class Collaboration(
    val id: String,
    val name: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val extensionElements: ExtensionElements,
    @XmlSerialName("participant", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpnm2")
    val participants: List<Participant>,
    @XmlSerialName("messageFlow", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpnm2")
    val messageFlow: MessageFlow?,
)
