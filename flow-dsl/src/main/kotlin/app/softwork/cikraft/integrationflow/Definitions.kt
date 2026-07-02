package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("definitions", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
public data class Definitions(
    val id: String,
    @XmlSerialName("collaboration", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val collaboration: Collaboration,
    @XmlSerialName("process", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn2")
    val process: Process,
    @XmlSerialName("BPMNDiagram", namespace = "http://www.omg.org/spec/BPMN/20100524/DI", "bpmndi")
    val diagram: Diagram? = null,
)

@Serializable
public data class Diagram(
    val id: String,
    val name: String,
    @XmlSerialName("BPMNPlane", namespace = "http://www.omg.org/spec/BPMN/20100524/DI", "bpmndi")
    val plane: Plane,
)

@Serializable
public data class Plane(
    val bpmnElement: String,
    val id: String,
    @XmlSerialName("BPMNShape", namespace = "http://www.omg.org/spec/BPMN/20100524/DI", "bpmndi")
    val shapes: List<Shape>,
    @XmlSerialName("BPMNEdge", namespace = "http://www.omg.org/spec/BPMN/20100524/DI", "bpmndi")
    val edges: List<Edge>,
)

@Serializable
public data class Shape(
    val bpmnElement: String,
    val id: String,
    @XmlSerialName("Bounds", namespace = "http://www.omg.org/spec/DD/20100524/DC", "dc")
    val bounds: Bounds,
)

@Serializable
public data class Bounds(val height: String, val width: String, val x: String, val y: String)

@Serializable
public data class Edge(
    val bpmnElement: String,
    val id: String,
    val sourceElement: String,
    val targetElement: String,
    @XmlSerialName("waypoint", namespace = "http://www.omg.org/spec/DD/20100524/DI", "di")
    val wayPoints: List<WayPoint>,
)

@Serializable
public data class WayPoint(
    val x: String,
    val y: String,
    @XmlSerialName("type", namespace = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi")
    val type: String = "dc:Point",
)
