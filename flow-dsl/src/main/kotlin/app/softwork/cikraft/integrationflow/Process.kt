package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class Process(
    val id: String,
    val name: String,
    @XmlSerialName("extensionElements", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpnm2")
    val extensionElements: ExtensionElements,
    @XmlSerialName("startEvent", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val startEvent: StartEvent,
    val callActivities: List<CallActivity>,
    val endEvent: EndEvent,
    val sequenceFlows: List<SequenceFlow>,
    @XmlSerialName("subProcess", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val subProcess: Process? = null,
)
