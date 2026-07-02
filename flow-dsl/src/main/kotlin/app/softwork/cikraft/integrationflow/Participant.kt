package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class Participant(
    val id: String,
    @XmlSerialName("type", namespace = "http:///com.sap.ifl.model/Ifl.xsd", "ifl")
    val type: String,
    val name: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val extensionElements: ExtensionElements = ExtensionElements(emptyList()),
    val processRef: String? = null,
)
