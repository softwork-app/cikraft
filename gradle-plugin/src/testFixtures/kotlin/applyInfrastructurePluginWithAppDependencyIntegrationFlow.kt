import app.softwork.cikraft.integrationflow.Bounds
import app.softwork.cikraft.integrationflow.CallActivity
import app.softwork.cikraft.integrationflow.Collaboration
import app.softwork.cikraft.integrationflow.Definitions
import app.softwork.cikraft.integrationflow.Diagram
import app.softwork.cikraft.integrationflow.Edge
import app.softwork.cikraft.integrationflow.EndEvent
import app.softwork.cikraft.integrationflow.ExtensionElements
import app.softwork.cikraft.integrationflow.MessageFlow
import app.softwork.cikraft.integrationflow.Participant
import app.softwork.cikraft.integrationflow.Plane
import app.softwork.cikraft.integrationflow.Process
import app.softwork.cikraft.integrationflow.Property
import app.softwork.cikraft.integrationflow.SequenceFlow
import app.softwork.cikraft.integrationflow.Shape
import app.softwork.cikraft.integrationflow.StartEvent
import app.softwork.cikraft.integrationflow.WayPoint

val applyInfrastructurePluginWithAppDependencyIntegrationFlow = Definitions(
    id = "Definitions",
    collaboration = Collaboration(
        id = "Collaboration",
        name = "Default Collaboration",
        extensionElements = ExtensionElements(
            listOf(
                Property("namespaceMapping", ""),
                Property("httpSessionHandling", "None"),
                Property("accessControlMaxAge", ""),
                Property("returnExceptionToSender", "false"),
                Property("log", "All events"),
                Property("corsEnabled", "false"),
                Property("exposedHeaders", ""),
                Property("componentVersion", "1.2"),
                Property("allowedHeaderList", "Accept|Content-Type|CCC"),
                Property("ServerTrace", "false"),
                Property("allowedOrigins", ""),
                Property("accessControlAllowCredentials", "false"),
                Property("allowedHeaders", ""),
                Property("allowedMethods", ""),
                Property(
                    "cmdVariantUri",
                    "ctype::IFlowVariant/cname::IFlowConfiguration/version::1.2.4",
                ),
            ),
        ),
        participants = listOf(
            Participant(
                id = "Participant",
                type = "EndpointSender",
                name = "Sender",
                extensionElements = ExtensionElements(
                    properties = listOf(
                        Property("enableBasicAuthentication", "false"),
                        Property("ifl:type", "EndpointSender"),
                    ),
                ),
            ),
            Participant(
                id = "Participant_Process",
                type = "IntegrationProcess",
                name = "Integration Process",
                processRef = "Process",
            ),
        ),
        messageFlow = MessageFlow(
            id = "MessageFlow",
            name = "HTTPS",
            sourceRef = "Participant",
            targetRef = "StartEvent",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("ComponentType", "HTTPS"),
                    Property("Description", ""),
                    Property("maximumBodySize", "40"),
                    Property("ComponentNS", "sap"),
                    Property("componentVersion", "1.5"),
                    Property("urlPath", "foo/foo/bar/baz"),
                    Property("Name", "HTTPS"),
                    Property("TransportProtocolVersion", "1.5.2"),
                    Property("ComponentSWCVName", "external"),
                    Property("system", "Sender"),
                    Property("xsrfProtection", "1"),
                    Property("TransportProtocol", "HTTPS"),
                    Property(
                        "cmdVariantUri",
                        "ctype::AdapterVariant/cname::sap:HTTPS/tp::HTTPS/mp::None/direction::Sender/version::1.5.2",
                    ),
                    Property("userRole", "SomeRole.send"),
                    Property("senderAuthType", "RoleBased"),
                    Property("MessageProtocol", "None"),
                    Property("MessageProtocolVersion", "1.5.2"),
                    Property("ComponentSWCVId", "1.5.2"),
                    Property("direction", "Sender"),
                ),
            ),
        ),
    ),
    process = Process(
        id = "Process",
        name = "Integration Process",
        extensionElements = ExtensionElements(
            properties = listOf(
                Property("transactionTimeout", "30"),
                Property("componentVersion", "1.2"),
                Property(
                    "cmdVariantUri",
                    "ctype::FlowElementVariant/cname::IntegrationProcess/version::1.2.1"
                ),
                Property("transactionalHandling", "Not Required"),
            ),
        ),
        callActivities = listOf(
            CallActivity(
                id = "ContentModifier_0",
                name = "1. Properties for test",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("bodyType", "expression"),
                        Property(
                            "propertyTable",
                            "<row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>{{a}}</cell><cell id='Default'></cell><cell id='Name'>a</cell><cell id='Datatype'></cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>{{b}}</cell><cell id='Default'></cell><cell id='Name'>b</cell><cell id='Datatype'></cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>{{d}}</cell><cell id='Default'></cell><cell id='Name'>d</cell><cell id='Datatype'></cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>{{e}}</cell><cell id='Default'></cell><cell id='Name'>e</cell><cell id='Datatype'></cell></row>",
                        ),
                        Property("headerTable", ""),
                        Property("wrapContent", ""),
                        Property("componentVersion", "1.6"),
                        Property("activityType", "Enricher"),
                        Property(
                            "cmdVariantUri",
                            "ctype::FlowstepVariant/cname::Enricher/version::1.6.0"
                        ),
                    ),
                ),
                incoming = "SequenceFlow_Start",
                outgoing = "SequenceFlow_0",
            ),
            CallActivity(
                id = "GroovyScript_1",
                name = "2. test",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "test"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property(
                            "cmdVariantUri",
                            "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"
                        ),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "entrypoints.groovy"),
                    ),
                ),
                incoming = "SequenceFlow_0",
                outgoing = "SequenceFlow_1",
            ),
            CallActivity(
                id = "Write2",
                name = "3. Write",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("storageName", "DS_Foo"),
                        Property("visibility", "global"),
                        Property("messageId", "MyEntryId"),
                        Property("alert", "2"),
                        Property("expire", "30"),
                        Property("encrypt", "false"),
                        Property("override", "false"),
                        Property("includeMessageHeaders", "false"),
                        Property("componentVersion", "1.7"),
                        Property("activityType", "DBstorage"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::put/version::1.7.1"),
                        Property("operation", "put"),
                    ),
                ),
                incoming = "SequenceFlow_1",
                outgoing = "SequenceFlow_End",
            ),
        ),
        startEvent = StartEvent(
            id = "StartEvent",
            name = "Start",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("componentVersion", "1.0"),
                    Property(
                        "cmdVariantUri",
                        "ctype::FlowstepVariant/cname::MessageStartEvent/version::1.0"
                    ),
                ),
            ),
            messageEventDefinition = "",
            outgoing = "SequenceFlow_Start",
        ),
        endEvent = EndEvent(
            id = "EndEvent",
            name = "End",
            extensionElements = ExtensionElements(
                listOf(
                    Property("componentVersion", "1.1"),
                    Property(
                        "cmdVariantUri",
                        "ctype::FlowstepVariant/cname::MessageEndEvent/version::1.1.0"
                    ),
                ),
            ),
            incoming = "SequenceFlow_End",
        ),
        sequenceFlows = listOf(
            SequenceFlow(
                "SequenceFlow_Start",
                "StartEvent",
                "ContentModifier_0",
            ),
            SequenceFlow(
                "SequenceFlow_0",
                "ContentModifier_0",
                "GroovyScript_1",
            ),
            SequenceFlow(
                "SequenceFlow_1",
                "GroovyScript_1",
                "Write2",
            ),
            SequenceFlow(
                "SequenceFlow_End",
                "Write2",
                "EndEvent",
            ),
        ),
    ),
    diagram = Diagram(
        id = "BPMNDiagram",
        name = "Default Collaboration Diagram",
        plane = Plane(
            bpmnElement = "Collaboration",
            id = "BPMNPlane",
            shapes = listOf(
                Shape(
                    bpmnElement = "Participant",
                    id = "BPMNShape_Participant",
                    bounds = Bounds(height = "140", width = "100", x = "0", y = "0"),
                ),
                Shape(
                    bpmnElement = "StartEvent",
                    id = "BPMNShape_StartEvent",
                    bounds = Bounds(height = "40", width = "40", x = "200", y = "50"),
                ),
                Shape(
                    bpmnElement = "ContentModifier_0",
                    id = "BPMNShape_ContentModifier_0",
                    bounds = Bounds(height = "60", width = "100", x = "300", y = "40"),
                ),
                Shape(
                    bpmnElement = "GroovyScript_1",
                    id = "BPMNShape_GroovyScript_1",
                    bounds = Bounds(height = "60", width = "100", x = "460", y = "40"),
                ),
                Shape(
                    bpmnElement = "Write2",
                    id = "BPMNShape_Write2",
                    bounds = Bounds(height = "60", width = "100", x = "620", y = "40"),
                ),
                Shape(
                    bpmnElement = "EndEvent",
                    id = "BPMNShape_EndEvent",
                    bounds = Bounds(height = "40", width = "40", x = "780", y = "50"),
                ),
                Shape(
                    bpmnElement = "Participant_Process",
                    id = "BPMNShape_Participant_Process",
                    bounds = Bounds(height = "140", width = "930", x = "150", y = "0"),
                ),
            ),
            edges = listOf(
                Edge(
                    bpmnElement = "SequenceFlow_Start",
                    id = "BPMNEdge_SequenceFlow_Start",
                    sourceElement = "BPMNShape_StartEvent",
                    targetElement = "BPMNShape_ContentModifier_0",
                    wayPoints = listOf(
                        WayPoint(x = "220", y = "70", type = "dc:Point"),
                        WayPoint(x = "300", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_0",
                    id = "BPMNEdge_SequenceFlow_0",
                    sourceElement = "BPMNShape_ContentModifier_0",
                    targetElement = "BPMNShape_GroovyScript_1",
                    wayPoints = listOf(
                        WayPoint(x = "380", y = "70", type = "dc:Point"),
                        WayPoint(x = "460", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_1",
                    id = "BPMNEdge_SequenceFlow_1",
                    sourceElement = "BPMNShape_GroovyScript_1",
                    targetElement = "BPMNShape_Write2",
                    wayPoints = listOf(
                        WayPoint(x = "540", y = "70", type = "dc:Point"),
                        WayPoint(x = "620", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_End",
                    id = "BPMNEdge_SequenceFlow_End",
                    sourceElement = "BPMNShape_Write2",
                    targetElement = "BPMNShape_EndEvent",
                    wayPoints = listOf(
                        WayPoint(x = "700", y = "70", type = "dc:Point"),
                        WayPoint(x = "780", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "MessageFlow",
                    id = "BPMNEdge_MessageFlow",
                    sourceElement = "BPMNShape_Participant",
                    targetElement = "BPMNShape_StartEvent",
                    wayPoints = listOf(
                        WayPoint(x = "90", y = "70", type = "dc:Point"),
                        WayPoint(x = "220", y = "70", type = "dc:Point"),
                    ),
                ),
            ),
        ),
    ),
)
