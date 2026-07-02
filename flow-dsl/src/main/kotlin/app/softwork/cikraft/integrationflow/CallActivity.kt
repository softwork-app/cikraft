package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("callActivity", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
public data class CallActivity(
    val id: String,
    val name: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val extensionElements: ExtensionElements,
    @XmlElement
    val incoming: String,
    @XmlElement
    val outgoing: String,
)
