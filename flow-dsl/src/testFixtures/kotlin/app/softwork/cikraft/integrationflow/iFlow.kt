package app.softwork.cikraft.integrationflow

val flow = Definitions(
    id = "Definitions",
    collaboration = Collaboration(
        id = "Collaboration",
        name = "Default Collaboration",
        extensionElements = ExtensionElements(
            listOf(
                Property("namespaceMapping", ""),
                Property("httpSessionHandling", "None"),
                Property("accessControlMaxAge", ""),
                Property("returnExceptionToSender", "true"),
                Property("log", "All events"),
                Property("corsEnabled", "false"),
                Property("exposedHeaders", ""),
                Property("componentVersion", "1.2"),
                Property("allowedHeaderList", ""),
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
                    Property("urlPath", "/Foo/01/50/4/1/7/5"),
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
                    Property("userRole", "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send"),
                    Property("senderAuthType", "RoleBased"),
                    Property("MessageProtocol", "None"),
                    Property("MessageProtocolVersion", "1.5.2"),
                    Property("ComponentSWCVId", "1.5.2"),
                    Property("direction", "Sender"),
                    Property("clientCertificates", ""),
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
                Property("cmdVariantUri", "ctype::FlowElementVariant/cname::IntegrationProcess/version::1.2.1"),
                Property("transactionalHandling", "Not Required"),
            ),
        ),
        callActivities = listOf(
            CallActivity(
                id = "ContentModifier_0",
                name = "Content Modifier",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("bodyType", "expression"),
                        Property(
                            "propertyTable",
                            "<row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>{{foo}}</cell><cell id='Default'></cell><cell id='Name'>foo</cell><cell id='Datatype'></cell></row><row><cell id='Action'>Create</cell><cell id='Type'>constant</cell><cell id='Value'>{{bar}}</cell><cell id='Default'></cell><cell id='Name'>bar</cell><cell id='Datatype'></cell></row>",
                        ),
                        Property("headerTable", ""),
                        Property("wrapContent", ""),
                        Property("componentVersion", "1.6"),
                        Property("activityType", "Enricher"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::Enricher/version::1.6.0"),
                    ),
                ),
                incoming = "SequenceFlow_Start",
                outgoing = "SequenceFlow_0",
            ),
            CallActivity(
                id = "GroovyScript_1",
                name = "Custom Groovy Script",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "convert"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "action.groovy"),
                    ),
                ),
                incoming = "SequenceFlow_0",
                outgoing = "SequenceFlow_End",
            ),
        ),
        startEvent = StartEvent(
            id = "StartEvent",
            name = "Start",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("componentVersion", "1.0"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageStartEvent/version::1.0"),
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
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageEndEvent/version::1.1.0"),
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
                "SequenceFlow_End",
                "GroovyScript_1",
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
                    bpmnElement = "EndEvent",
                    id = "BPMNShape_EndEvent",
                    bounds = Bounds(height = "40", width = "40", x = "620", y = "50"),
                ),
                Shape(
                    bpmnElement = "Participant_Process",
                    id = "BPMNShape_Participant_Process",
                    bounds = Bounds(height = "140", width = "770", x = "150", y = "0"),
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
                    bpmnElement = "SequenceFlow_End",
                    id = "BPMNEdge_SequenceFlow_End",
                    sourceElement = "BPMNShape_GroovyScript_1",
                    targetElement = "BPMNShape_EndEvent",
                    wayPoints = listOf(
                        WayPoint(x = "540", y = "70", type = "dc:Point"),
                        WayPoint(x = "620", y = "70", type = "dc:Point"),
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

val exceptionHandling = Definitions(
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
                Property("allowedHeaderList", ""),
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
                name = "Foo",
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
                    Property("urlPath", "/Foo/exceptions"),
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
                    Property("userRole", "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send"),
                    Property("senderAuthType", "RoleBased"),
                    Property("MessageProtocol", "None"),
                    Property("MessageProtocolVersion", "1.5.2"),
                    Property("ComponentSWCVId", "1.5.2"),
                    Property("direction", "Sender"),
                    Property("clientCertificates", ""),
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
                Property("cmdVariantUri", "ctype::FlowElementVariant/cname::IntegrationProcess/version::1.2.1"),
                Property("transactionalHandling", "Not Required"),
            ),
        ),
        startEvent = StartEvent(
            id = "StartEvent",
            name = "CustomStart",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("componentVersion", "1.0"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageStartEvent/version::1.0"),
                ),
            ),
            messageEventDefinition = "",
            outgoing = "SequenceFlow_Start",
        ),
        callActivities = listOf(
            CallActivity(
                id = "GroovyScript_0",
                name = "Custom Groovy Script",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "convert1"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "action.groovy"),
                    ),
                ),
                incoming = "SequenceFlow_Start",
                outgoing = "SequenceFlow_End",
            ),
        ),
        endEvent = EndEvent(
            id = "EndEvent",
            name = "CustomEnd",
            extensionElements = ExtensionElements(
                listOf(
                    Property("componentVersion", "1.1"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageEndEvent/version::1.1.0"),
                ),
            ),
            incoming = "SequenceFlow_End",
        ),
        sequenceFlows = listOf(
            SequenceFlow(
                "SequenceFlow_Start",
                "StartEvent",
                "GroovyScript_0",
            ),
            SequenceFlow(
                "SequenceFlow_End",
                "GroovyScript_0",
                "EndEvent",
            ),
        ),
        subProcess = Process(
            id = "SubProcess",
            name = "Exception Subprocess",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("componentVersion", "1.1"),
                    Property("activityType", "ErrorEventSubProcessTemplate"),
                    Property(
                        "cmdVariantUri",
                        "ctype::FlowstepVariant/cname::ErrorEventSubProcessTemplate/version::1.1.0"
                    ),
                ),
            ),
            startEvent = StartEvent(
                id = "StartEvent_ExceptionSubProcess",
                name = "ErrorStartHandling",
                extensionElements = null,
                outgoing = "SequenceFlow_Start_ExceptionSubProcess",
                messageEventDefinition = null,
                errorEventDefinition = EndEvent.ErrorEventDefinition(
                    extensionElements = ExtensionElements(
                        listOf(
                            Property("cmdVariantUri", "ctype::FlowstepVariant/cname::ErrorStartEvent"),
                            Property("activityType", "StartErrorEvent"),
                        )
                    )
                )
            ),
            callActivities = listOf(
                CallActivity(
                    id = "GroovyScript_0_ExceptionSubProcess",
                    name = "Custom Groovy Script",
                    extensionElements = ExtensionElements(
                        listOf(
                            Property("scriptFunction", "convert1"),
                            Property("scriptBundleId", ""),
                            Property("componentVersion", "1.1"),
                            Property("activityType", "Script"),
                            Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                            Property("subActivityType", "GroovyScript"),
                            Property("script", "action.groovy"),
                        ),
                    ),
                    incoming = "SequenceFlow_Start_ExceptionSubProcess",
                    outgoing = "SequenceFlow_End_ExceptionSubProcess",
                ),
            ),
            endEvent = EndEvent(
                id = "EndEvent_ExceptionSubProcess",
                name = "ErrorEndHandling",
                extensionElements = null,
                incoming = "SequenceFlow_End_ExceptionSubProcess",
                messageEventDefinition = null,
                errorEventDefinition = EndEvent.ErrorEventDefinition(
                    extensionElements = ExtensionElements(
                        listOf(
                            Property("cmdVariantUri", "ctype::FlowstepVariant/cname::ErrorEndEvent"),
                            Property("activityType", "EndErrorEvent"),
                        )
                    )
                )
            ),
            sequenceFlows = listOf(
                SequenceFlow(
                    "SequenceFlow_Start_ExceptionSubProcess",
                    "StartEvent_ExceptionSubProcess",
                    "GroovyScript_0_ExceptionSubProcess",
                ),
                SequenceFlow(
                    "SequenceFlow_End_ExceptionSubProcess",
                    "GroovyScript_0_ExceptionSubProcess",
                    "EndEvent_ExceptionSubProcess",
                ),
            ),
        )
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
                    bpmnElement = "GroovyScript_0",
                    id = "BPMNShape_GroovyScript_0",
                    bounds = Bounds(height = "60", width = "100", x = "300", y = "40"),
                ),
                Shape(
                    bpmnElement = "EndEvent",
                    id = "BPMNShape_EndEvent",
                    bounds = Bounds(height = "40", width = "40", x = "460", y = "50"),
                ),
                Shape(
                    bpmnElement = "Participant_Process",
                    id = "BPMNShape_Participant_Process",
                    bounds = Bounds(height = "300", width = "610", x = "150", y = "0"),
                ),
                Shape(
                    bpmnElement = "StartEvent_ExceptionSubProcess",
                    id = "BPMNShape_StartEvent_ExceptionSubProcess",
                    bounds = Bounds(height = "40", width = "40", x = "200", y = "200")
                ),
                Shape(
                    bpmnElement = "GroovyScript_0_ExceptionSubProcess",
                    id = "BPMNShape_GroovyScript_0_ExceptionSubProcess",
                    bounds = Bounds(height = "60", width = "100", x = "300", y = "190")
                ),
                Shape(
                    bpmnElement = "EndEvent_ExceptionSubProcess",
                    id = "BPMNShape_EndEvent_ExceptionSubProcess",
                    bounds = Bounds(height = "40", width = "40", x = "460", y = "200")
                ),
                Shape(
                    bpmnElement = "SubProcess",
                    id = "BPMNShape_ExceptionSubProcess",
                    bounds = Bounds(height = "140", width = "610", x = "150", y = "150")
                )
            ),
            edges = listOf(
                Edge(
                    bpmnElement = "SequenceFlow_Start",
                    id = "BPMNEdge_SequenceFlow_Start",
                    sourceElement = "BPMNShape_StartEvent",
                    targetElement = "BPMNShape_GroovyScript_0",
                    wayPoints = listOf(
                        WayPoint(x = "220", y = "70", type = "dc:Point"),
                        WayPoint(x = "300", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_End",
                    id = "BPMNEdge_SequenceFlow_End",
                    sourceElement = "BPMNShape_GroovyScript_0",
                    targetElement = "BPMNShape_EndEvent",
                    wayPoints = listOf(
                        WayPoint(x = "380", y = "70", type = "dc:Point"),
                        WayPoint(x = "460", y = "70", type = "dc:Point"),
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
                Edge(
                    bpmnElement = "SequenceFlow_Start_ExceptionSubProcess",
                    id = "BPMNEdge_SequenceFlow_Start_ExceptionSubProcess",
                    sourceElement = "BPMNShape_StartEvent_ExceptionSubProcess",
                    targetElement = "BPMNShape_GroovyScript_0_ExceptionSubProcess",
                    wayPoints = listOf(
                        WayPoint(x = "220", y = "220", type = "dc:Point"),
                        WayPoint(x = "300", y = "220", type = "dc:Point")
                    )
                ),
                Edge(
                    bpmnElement = "SequenceFlow_End_ExceptionSubProcess",
                    id = "BPMNEdge_SequenceFlow_End_ExceptionSubProcess",
                    sourceElement = "BPMNShape_GroovyScript_0_ExceptionSubProcess",
                    targetElement = "BPMNShape_EndEvent_ExceptionSubProcess",
                    wayPoints = listOf(
                        WayPoint(x = "380", y = "220", type = "dc:Point"),
                        WayPoint(x = "460", y = "220", type = "dc:Point")
                    )
                ),
            ),
        ),
    )
)

val emptyExceptionHandling = Definitions(
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
                Property("allowedHeaderList", ""),
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
                name = "Foo",
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
                    Property("urlPath", "/Foo/exceptions"),
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
                    Property("userRole", "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send"),
                    Property("senderAuthType", "RoleBased"),
                    Property("MessageProtocol", "None"),
                    Property("MessageProtocolVersion", "1.5.2"),
                    Property("ComponentSWCVId", "1.5.2"),
                    Property("direction", "Sender"),
                    Property("clientCertificates", ""),
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
                Property("cmdVariantUri", "ctype::FlowElementVariant/cname::IntegrationProcess/version::1.2.1"),
                Property("transactionalHandling", "Not Required"),
            ),
        ),
        startEvent = StartEvent(
            id = "StartEvent",
            name = "CustomStart",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("componentVersion", "1.0"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageStartEvent/version::1.0"),
                ),
            ),
            messageEventDefinition = "",
            outgoing = "SequenceFlow_Start",
        ),
        callActivities = listOf(
            CallActivity(
                id = "GroovyScript_0",
                name = "Custom Groovy Script",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "convert1"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "action.groovy"),
                    ),
                ),
                incoming = "SequenceFlow_Start",
                outgoing = "SequenceFlow_End",
            ),
        ),
        endEvent = EndEvent(
            id = "EndEvent",
            name = "CustomEnd",
            extensionElements = ExtensionElements(
                listOf(
                    Property("componentVersion", "1.1"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageEndEvent/version::1.1.0"),
                ),
            ),
            incoming = "SequenceFlow_End",
        ),
        sequenceFlows = listOf(
            SequenceFlow(
                "SequenceFlow_Start",
                "StartEvent",
                "GroovyScript_0",
            ),
            SequenceFlow(
                "SequenceFlow_End",
                "GroovyScript_0",
                "EndEvent",
            ),
        ),
        subProcess = Process(
            id = "SubProcess",
            name = "Exception Subprocess",
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("componentVersion", "1.1"),
                    Property("activityType", "ErrorEventSubProcessTemplate"),
                    Property(
                        "cmdVariantUri",
                        "ctype::FlowstepVariant/cname::ErrorEventSubProcessTemplate/version::1.1.0"
                    ),
                ),
            ),
            startEvent = StartEvent(
                id = "StartEvent_ExceptionSubProcess",
                name = "ErrorStartHandling",
                extensionElements = null,
                outgoing = "SequenceFlow_Start_ExceptionSubProcess",
                messageEventDefinition = null,
                errorEventDefinition = EndEvent.ErrorEventDefinition(
                    extensionElements = ExtensionElements(
                        listOf(
                            Property("cmdVariantUri", "ctype::FlowstepVariant/cname::ErrorStartEvent"),
                            Property("activityType", "StartErrorEvent"),
                        )
                    )
                )
            ),
            callActivities = emptyList(),
            endEvent = EndEvent(
                id = "EndEvent_ExceptionSubProcess",
                name = "ErrorEndHandling",
                extensionElements = null,
                incoming = "SequenceFlow_Start_ExceptionSubProcess",
                messageEventDefinition = null,
                errorEventDefinition = EndEvent.ErrorEventDefinition(
                    extensionElements = ExtensionElements(
                        listOf(
                            Property("cmdVariantUri", "ctype::FlowstepVariant/cname::ErrorEndEvent"),
                            Property("activityType", "EndErrorEvent"),
                        )
                    )
                )
            ),
            sequenceFlows = listOf(
                SequenceFlow(
                    "SequenceFlow_Start_ExceptionSubProcess",
                    "StartEvent_ExceptionSubProcess",
                    "EndEvent_ExceptionSubProcess",
                ),
            ),
        )
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
                    bpmnElement = "GroovyScript_0",
                    id = "BPMNShape_GroovyScript_0",
                    bounds = Bounds(height = "60", width = "100", x = "300", y = "40"),
                ),
                Shape(
                    bpmnElement = "EndEvent",
                    id = "BPMNShape_EndEvent",
                    bounds = Bounds(height = "40", width = "40", x = "460", y = "50"),
                ),
                Shape(
                    bpmnElement = "Participant_Process",
                    id = "BPMNShape_Participant_Process",
                    bounds = Bounds(height = "300", width = "610", x = "150", y = "0"),
                ),
                Shape(
                    bpmnElement = "StartEvent_ExceptionSubProcess",
                    id = "BPMNShape_StartEvent_ExceptionSubProcess",
                    bounds = Bounds(height = "40", width = "40", x = "200", y = "200")
                ),
                Shape(
                    bpmnElement = "EndEvent_ExceptionSubProcess",
                    id = "BPMNShape_EndEvent_ExceptionSubProcess",
                    bounds = Bounds(height = "40", width = "40", x = "300", y = "200")
                ),
                Shape(
                    bpmnElement = "SubProcess",
                    id = "BPMNShape_ExceptionSubProcess",
                    bounds = Bounds(height = "140", width = "450", x = "150", y = "150")
                )
            ),
            edges = listOf(
                Edge(
                    bpmnElement = "SequenceFlow_Start",
                    id = "BPMNEdge_SequenceFlow_Start",
                    sourceElement = "BPMNShape_StartEvent",
                    targetElement = "BPMNShape_GroovyScript_0",
                    wayPoints = listOf(
                        WayPoint(x = "220", y = "70", type = "dc:Point"),
                        WayPoint(x = "300", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_End",
                    id = "BPMNEdge_SequenceFlow_End",
                    sourceElement = "BPMNShape_GroovyScript_0",
                    targetElement = "BPMNShape_EndEvent",
                    wayPoints = listOf(
                        WayPoint(x = "380", y = "70", type = "dc:Point"),
                        WayPoint(x = "460", y = "70", type = "dc:Point"),
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
                Edge(
                    bpmnElement = "SequenceFlow_Start_ExceptionSubProcess",
                    id = "BPMNEdge_SequenceFlow_Start_ExceptionSubProcess",
                    sourceElement = "BPMNShape_StartEvent_ExceptionSubProcess",
                    targetElement = "BPMNShape_EndEvent_ExceptionSubProcess",
                    wayPoints = listOf(
                        WayPoint(x = "220", y = "220", type = "dc:Point"),
                        WayPoint(x = "300", y = "220", type = "dc:Point")
                    )
                ),
            ),
        ),
    )
)

val flowMultipleSteps = Definitions(
    id = "Definitions",
    collaboration = Collaboration(
        id = "Collaboration",
        name = "Default Collaboration",
        extensionElements = ExtensionElements(
            listOf(
                Property("namespaceMapping", ""),
                Property("httpSessionHandling", "None"),
                Property("accessControlMaxAge", ""),
                Property("returnExceptionToSender", "true"),
                Property("log", "All events"),
                Property("corsEnabled", "false"),
                Property("exposedHeaders", ""),
                Property("componentVersion", "1.2"),
                Property("allowedHeaderList", ""),
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
                    Property("urlPath", "/Foo/01/50/4/1/7/5"),
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
                    Property("userRole", "AAA_Foo_ISCI_TEC_RO_ESBMessaging.send"),
                    Property("senderAuthType", "RoleBased"),
                    Property("MessageProtocol", "None"),
                    Property("MessageProtocolVersion", "1.5.2"),
                    Property("ComponentSWCVId", "1.5.2"),
                    Property("direction", "Sender"),
                    Property("clientCertificates", ""),
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
                Property("cmdVariantUri", "ctype::FlowElementVariant/cname::IntegrationProcess/version::1.2.1"),
                Property("transactionalHandling", "Not Required"),
            ),
        ),
        callActivities = listOf(
            CallActivity(
                id = "GroovyScript_0",
                name = "Custom Groovy Script",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "convert1"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "action.groovy"),
                    ),
                ),
                incoming = "SequenceFlow_Start",
                outgoing = "SequenceFlow_0",
            ),
            CallActivity(
                id = "GroovyScript_1",
                name = "Custom Groovy Script",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "convert2"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "action.groovy"),
                    ),
                ),
                incoming = "SequenceFlow_0",
                outgoing = "SequenceFlow_1",
            ),
            CallActivity(
                id = "GroovyScript_2",
                name = "Custom Groovy Script",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("scriptFunction", "convert3"),
                        Property("scriptBundleId", ""),
                        Property("componentVersion", "1.1"),
                        Property("activityType", "Script"),
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                        Property("subActivityType", "GroovyScript"),
                        Property("script", "action.groovy"),
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
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageStartEvent/version::1.0"),
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
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageEndEvent/version::1.1.0"),
                ),
            ),
            incoming = "SequenceFlow_End",
        ),
        sequenceFlows = listOf(
            SequenceFlow(
                "SequenceFlow_Start",
                "StartEvent",
                "GroovyScript_0",
            ),
            SequenceFlow(
                "SequenceFlow_0",
                "GroovyScript_0",
                "GroovyScript_1",
            ),
            SequenceFlow(
                "SequenceFlow_1",
                "GroovyScript_1",
                "GroovyScript_2",
            ),
            SequenceFlow(
                "SequenceFlow_End",
                "GroovyScript_2",
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
                    bpmnElement = "GroovyScript_0",
                    id = "BPMNShape_GroovyScript_0",
                    bounds = Bounds(height = "60", width = "100", x = "300", y = "40"),
                ),
                Shape(
                    bpmnElement = "GroovyScript_1",
                    id = "BPMNShape_GroovyScript_1",
                    bounds = Bounds(height = "60", width = "100", x = "460", y = "40"),
                ),
                Shape(
                    bpmnElement = "GroovyScript_2",
                    id = "BPMNShape_GroovyScript_2",
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
                    targetElement = "BPMNShape_GroovyScript_0",
                    wayPoints = listOf(
                        WayPoint(x = "220", y = "70", type = "dc:Point"),
                        WayPoint(x = "300", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_0",
                    id = "BPMNEdge_SequenceFlow_0",
                    sourceElement = "BPMNShape_GroovyScript_0",
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
                    targetElement = "BPMNShape_GroovyScript_2",
                    wayPoints = listOf(
                        WayPoint(x = "540", y = "70", type = "dc:Point"),
                        WayPoint(x = "620", y = "70", type = "dc:Point"),
                    ),
                ),
                Edge(
                    bpmnElement = "SequenceFlow_End",
                    id = "BPMNEdge_SequenceFlow_End",
                    sourceElement = "BPMNShape_GroovyScript_2",
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
