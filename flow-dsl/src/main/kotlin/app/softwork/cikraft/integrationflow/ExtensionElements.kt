package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class ExtensionElements(
    @XmlSerialName("property", namespace = "http:///com.sap.ifl.model/Ifl.xsd", "ifl")
    val properties: List<Property>,
)
