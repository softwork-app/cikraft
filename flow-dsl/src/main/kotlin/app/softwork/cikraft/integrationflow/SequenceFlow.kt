package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("sequenceFlow", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
public data class SequenceFlow(val id: String, val sourceRef: String, val targetRef: String)
