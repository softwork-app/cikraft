package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class MessageFlow(
    val id: String,
    val name: String,
    val sourceRef: String,
    val targetRef: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val extensionElements: ExtensionElements,
)
