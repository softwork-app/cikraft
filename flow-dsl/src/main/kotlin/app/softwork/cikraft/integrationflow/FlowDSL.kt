package app.softwork.cikraft.integrationflow

import org.intellij.lang.annotations.Language
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@DslMarker
public annotation class FlowDSL

public fun Config.integrationFlow(
    builder: EndpointBuilder.() -> Unit,
): Definitions {
    val endpointBuilder = EndpointBuilder(this).apply(builder)
    val senderParticipant = endpointBuilder.senderParticipant()
    val endpointMessageFlow = endpointBuilder.buildMessageFlow()

    val participantProcess = Participant(
        id = "Participant_Process",
        type = "IntegrationProcess",
        name = "Integration Process",
        processRef = "Process",
    )

    val collaboration1 = Collaboration(
        id = "Collaboration",
        name = "Default Collaboration",
        extensionElements = ExtensionElements(
            listOf(
                Property("namespaceMapping", ""),
                Property("httpSessionHandling", "None"),
                Property("accessControlMaxAge", ""),
                Property(
                    "returnExceptionToSender",
                    when (val entry = endpointBuilder.entry) {
                        is Entry.HttpsBuilder -> entry.returnExceptionsToSender.toString()
                        is Entry.Timer -> "false"
                        is Entry.DataStore -> "false"
                    },
                ),
                Property("log", "All events"),
                Property("corsEnabled", "false"),
                Property("exposedHeaders", ""),
                Property("componentVersion", "1.2"),
                Property("allowedHeaderList", allowedHeaders.joinToString("|")),
                Property("ServerTrace", "false"),
                // CORS relevant properties: https://help.sap.com/docs/integration-suite/sap-integration-suite/cors-support?locale=en-US
                Property("allowedOrigins", ""),
                Property("accessControlAllowCredentials", "false"),
                Property("allowedHeaders", ""),
                Property("allowedMethods", ""), // Uppercase method name
                Property(
                    "cmdVariantUri",
                    "ctype::IFlowVariant/cname::IFlowConfiguration/version::1.2.4",
                ),
            ),
        ),
        participants = listOfNotNull(
            senderParticipant,
            participantProcess,
        ),
        messageFlow = endpointMessageFlow,
    )

    val sequenceFlows = endpointBuilder.entry.stepBuilder.buildSequenceFlows("")
    val process = endpointBuilder.entry.toProcess()
    val prefix = "BPMNShape_"
    val definitions = Definitions(
        id = "Definitions",
        collaboration = collaboration1,
        process = process,
        diagram = Diagram(
            id = "BPMNDiagram",
            name = "Default Collaboration Diagram",
            plane = Plane(
                bpmnElement = collaboration1.id,
                id = "BPMNPlane",
                shapes = buildList {
                    if (senderParticipant != null) {
                        add(
                            Shape(
                                bpmnElement = senderParticipant.id,
                                id = prefix + senderParticipant.id,
                                bounds = Bounds(
                                    height = "140",
                                    width = "100",
                                    x = "0",
                                    y = "0",
                                ),
                            ),
                        )
                    }

                    val x = addCallActivities(endpointBuilder.entry.stepBuilder.callActivities, "", yOffSet = 0)

                    add(
                        Shape(
                            bpmnElement = participantProcess.id,
                            id = prefix + participantProcess.id,
                            bounds = Bounds(
                                height = if (endpointBuilder.entry.stepBuilder.subprocess == null) {
                                    "140"
                                } else {
                                    "300"
                                },
                                width = "${x + 150}",
                                x = "150",
                                y = "0",
                            ),
                        ),
                    )

                    if (endpointBuilder.entry.stepBuilder.subprocess != null) {
                        val x = addCallActivities(
                            endpointBuilder.entry.stepBuilder.subprocess!!.callActivities,
                            "_${EXCEPTION_SUBPROCESS}",
                            yOffSet = 150,
                        )

                        add(
                            Shape(
                                bpmnElement = "SubProcess",
                                id = prefix + EXCEPTION_SUBPROCESS,
                                bounds = Bounds(
                                    height = "140",
                                    width = "${x + 150}",
                                    x = "150",
                                    y = "150",
                                ),
                            ),
                        )
                    }
                },
                edges = buildList {
                    for ((index, sequenceFlow) in sequenceFlows.withIndex()) {
                        add(
                            Edge(
                                bpmnElement = sequenceFlow.id,
                                id = "BPMNEdge_" + sequenceFlow.id,
                                sourceElement = prefix + sequenceFlow.sourceRef,
                                targetElement = prefix + sequenceFlow.targetRef,
                                wayPoints = listOf(
                                    WayPoint(x = "${220 + (index * 160)}", y = "70"),
                                    WayPoint(x = "${300 + (index * 160)}", y = "70"),
                                ),
                            ),
                        )
                    }
                    if (endpointMessageFlow != null && senderParticipant != null) {
                        add(
                            Edge(
                                bpmnElement = endpointMessageFlow.id,
                                id = "BPMNEdge_" + endpointMessageFlow.id,
                                sourceElement = prefix + senderParticipant.id,
                                targetElement = prefix + "StartEvent",
                                wayPoints = listOf(
                                    WayPoint(x = "90", y = "70"),
                                    WayPoint(x = "220", y = "70"),
                                ),
                            ),
                        )
                    }

                    val errorSequenceFlow =
                        endpointBuilder.entry.stepBuilder.subprocess?.buildSequenceFlows("_$EXCEPTION_SUBPROCESS")

                    if (errorSequenceFlow != null) {
                        for ((index, sequenceFlow) in errorSequenceFlow.withIndex()) {
                            add(
                                Edge(
                                    bpmnElement = sequenceFlow.id,
                                    id = "BPMNEdge_" + sequenceFlow.id,
                                    sourceElement = prefix + sequenceFlow.sourceRef,
                                    targetElement = prefix + sequenceFlow.targetRef,
                                    wayPoints = listOf(
                                        WayPoint(x = "${220 + (index * 160)}", y = "220"),
                                        WayPoint(x = "${300 + (index * 160)}", y = "220"),
                                    ),
                                ),
                            )
                        }
                    }
                },
            ),
        ),
    )

    return definitions
}

private fun MutableList<Shape>.addCallActivities(
    callActivities: List<CallActivity>,
    suffix: String,
    yOffSet: Int,
): Int {
    add(
        Shape(
            bpmnElement = "StartEvent$suffix",
            id = "BPMNShape_StartEvent$suffix",
            bounds = Bounds(
                height = "40",
                width = "40",
                x = "200",
                y = (yOffSet + 50).toString(),
            ),
        ),
    )

    var x = 300

    for (callActivity in callActivities) {
        add(
            Shape(
                bpmnElement = callActivity.id,
                id = "BPMNShape_${callActivity.id}",
                bounds = Bounds(
                    height = "60",
                    width = "100",
                    x = "$x",
                    y = (yOffSet + 40).toString(),
                ),
            ),
        )
        x += 160
    }

    add(
        Shape(
            bpmnElement = "EndEvent$suffix",
            id = "BPMNShape_EndEvent$suffix",
            bounds = Bounds(
                height = "40",
                width = "40",
                x = "$x",
                y = (yOffSet + 50).toString(),
            ),
        ),
    )
    return x
}

public interface Config {
    public val baseUrl: String
    public val allowedHeaders: Set<String>
}

private fun buildXml(xmlBuilder: XmlBuilder.() -> Unit): String = buildString {
    XmlBuilder(this).apply(xmlBuilder)
}

private class XmlBuilder(private val stringBuilder: StringBuilder) {
    fun row(rowBuilder: RowBuilder.() -> Unit) {
        stringBuilder.append("<row>")
        RowBuilder(stringBuilder).rowBuilder()
        stringBuilder.append("</row>")
    }

    class RowBuilder(private val stringBuilder: StringBuilder) {
        fun cell(value: String, id: String? = null) {
            stringBuilder.append("<cell")
            if (id != null) {
                stringBuilder.append(" id='$id'")
            }
            stringBuilder.append(">")
            stringBuilder.append(value)
            stringBuilder.append("</cell>")
        }
    }
}

@FlowDSL
public class EndpointBuilder internal constructor(private val config: Config) {
    public var sender: String = "Sender"
    public var receiver: String = "Receiver"

    private val id = "Participant"

    internal lateinit var entry: Entry

    public fun https(
        url: String,
        userRole: String,
        xsrfProtection: Boolean = false,
        clientCertificates: Boolean = false,
        maximumBodySize: MB = 40.MB,
        returnExceptionsToSender: Boolean = false,
        stepBuilder: StepBuilder.NeedsStartMessage.() -> Unit,
    ) {
        entry = Entry.HttpsBuilder(
            url = config.baseUrl + url,
            userRole = userRole,
            xsrfProtection = xsrfProtection,
            clientCertificates = clientCertificates,
            returnExceptionsToSender = returnExceptionsToSender,
            maximumBodySize = maximumBodySize.value,
            stepBuilder = StepBuilder.NeedsStartMessage(suffix = "").apply(stepBuilder),
        )
    }

    /**
     * See also [CronMaker](http://www.cronmaker.com)
     */
    public fun timer(
        @Language("CronExp") quartzCron: String,
        stepBuilder: StepBuilder.() -> Unit,
    ) {
        entry = Entry.Timer.Scheduled(
            quartzCron = quartzCron,
            stepBuilder = StepBuilder.Normal().apply {
                startMessage = StepBuilder.TimerStartMessage
                stepBuilder()
            },
        )
    }

    public fun dataStore(
        dataStoreName: String,
        visibility: DataStoreVisibility = DataStoreVisibility.IntegrationFlow,
        pollInterval: Duration = 10.seconds,
        retryInterval: Duration = 1.minutes,
        exponentialBackoff: Boolean = false,
        maximumRetryInterval: Duration = 1.hours,
        lockTimeout: Duration = 10.minutes,
        stepBuilder: StepBuilder.NeedsStartMessage.() -> Unit,
    ) {
        entry = Entry.DataStore(
            dataStoreName = dataStoreName,
            visibility = visibility,
            pollInterval = pollInterval,
            retryInterval = retryInterval,
            exponentialBackoff = exponentialBackoff,
            maximumRetryInterval = maximumRetryInterval,
            lockTimeout = lockTimeout,
            StepBuilder.NeedsStartMessage(suffix = "").apply(stepBuilder),
        )
    }

    internal fun senderParticipant(): Participant? = when (entry) {
        is Entry.HttpsBuilder,
        -> Participant(
            id = id,
            type = "EndpointSender",
            name = sender,
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("enableBasicAuthentication", "false"),
                    Property("ifl:type", "EndpointSender"),
                ),
            ),
        )

        is Entry.DataStore,
        -> Participant(
            id = id,
            type = "EndpointSender",
            name = sender,
            extensionElements = ExtensionElements(
                properties = listOf(
                    Property("ifl:type", "EndpointSender"),
                ),
            ),
        )

        is Entry.Timer -> null
    }

    internal fun buildMessageFlow(): MessageFlow? = when (val entry = entry) {
        is Entry.HttpsBuilder -> MessageFlow(
            id = "MessageFlow",
            name = "HTTPS",
            sourceRef = id,
            targetRef = "StartEvent",
            extensionElements = ExtensionElements(
                properties = buildList {
                    add(Property("ComponentType", "HTTPS"))
                    add(Property("Description", ""))
                    add(Property("maximumBodySize", entry.maximumBodySize.toString()))
                    add(Property("ComponentNS", "sap"))
                    add(Property("componentVersion", "1.5"))
                    add(Property("urlPath", entry.url))
                    add(Property("Name", "HTTPS"))
                    add(Property("TransportProtocolVersion", "1.5.2"))
                    add(Property("ComponentSWCVName", "external"))
                    add(Property("system", "Sender"))
                    add(Property("xsrfProtection", if (entry.xsrfProtection) "1" else "0"))
                    add(Property("TransportProtocol", "HTTPS"))
                    add(
                        Property(
                            "cmdVariantUri",
                            "ctype::AdapterVariant/cname::sap:HTTPS/tp::HTTPS/mp::None/direction::Sender/version::1.5.2",
                        ),
                    )
                    add(Property("userRole", entry.userRole))
                    add(Property("senderAuthType", "RoleBased"))
                    add(Property("MessageProtocol", "None"))
                    add(Property("MessageProtocolVersion", "1.5.2"))
                    add(Property("ComponentSWCVId", "1.5.2"))
                    add(Property("direction", "Sender"))

                    if (entry.clientCertificates) {
                        add(
                            Property("clientCertificates", ""),
                        )
                    }
                },
            ),
        )

        is Entry.DataStore -> MessageFlow(
            id = "MessageFlow",
            name = "DataStore",
            sourceRef = id,
            targetRef = "StartEvent",
            extensionElements = ExtensionElements(
                properties = buildList {
                    add(Property("ComponentType", "DataStoreConsumer"))
                    add(Property("Description", ""))
                    add(Property("visibility", entry.visibility.id))
                    add(Property("MaxRetryInterval", entry.maximumRetryInterval.inWholeMinutes.toString()))
                    add(Property("ComponentNS", "sap"))
                    add(Property("componentVersion", "1.0"))
                    add(Property("ExponentialBackoff", if (entry.exponentialBackoff) "1" else "0"))
                    add(Property("Name", "DataStore"))
                    add(Property("storageName", entry.dataStoreName))
                    add(Property("TransportProtocolVersion", "1.0.0"))
                    add(Property("ComponentSWCVName", "external"))
                    add(Property("system", "Sender"))
                    add(Property("TransportProtocol", "JDBC"))
                    add(Property("PollDelay", entry.pollInterval.inWholeSeconds.toString()))
                    add(Property("RetryInterval", entry.retryInterval.inWholeMinutes.toString()))
                    add(
                        Property(
                            "cmdVariantUri",
                            "ctype::AdapterVariant/cname::sap:DataStoreConsumer/tp::JDBC/mp::None/direction::Sender/version::1.0.0",
                        ),
                    )
                    add(Property("MessageProtocol", "None"))
                    add(Property("MessageProtocolVersion", "1.0.0"))
                    add(Property("ComponentSWCVId", "1.0.0"))
                    add(Property("file_lock_timeout", entry.lockTimeout.inWholeMinutes.toString()))
                    add(Property("direction", "Sender"))
                },
            ),
        )

        is Entry.Timer -> null
    }
}

private fun StepBuilder.buildSequenceFlows(suffix: String): List<SequenceFlow> = buildList {
    add(
        SequenceFlow(
            "SequenceFlow_Start$suffix",
            "StartEvent$suffix",
            targetRef = if (callActivities.isEmpty()) "EndEvent$suffix" else callActivities.first().id,
        ),
    )

    if (callActivities.size != 1) {
        for ((index, callActivity) in callActivities.withIndex().drop(1)) {
            add(
                SequenceFlow(
                    id = "SequenceFlow_${index - 1}$suffix",
                    sourceRef = callActivities[index - 1].id,
                    targetRef = callActivity.id,
                ),
            )
        }
    }

    if (callActivities.isNotEmpty()) {
        add(
            SequenceFlow(
                "SequenceFlow_End$suffix",
                callActivities.last().id,
                "EndEvent$suffix",
            ),
        )
    }
}

internal sealed interface Entry {
    val stepBuilder: StepBuilder

    data class HttpsBuilder(
        val url: String,
        val userRole: String,
        val xsrfProtection: Boolean,
        val clientCertificates: Boolean,
        val maximumBodySize: Int,
        val returnExceptionsToSender: Boolean,
        override val stepBuilder: StepBuilder,
    ) : Entry

    sealed interface Timer : Entry {
        data class Scheduled(val quartzCron: String, override val stepBuilder: StepBuilder) : Timer {
            private val cronParts = quartzCron.split(" ")
            val second: String = cronParts[0]
            val minute: String = cronParts[1]
            val hour: String = cronParts[2]
            val dayOfMonth: String = cronParts[3]
            val month: String = cronParts[4]
            val dayOfWeek: String = cronParts[5]
            val year: String = cronParts.getOrNull(6) ?: "*"
        }
    }

    data class DataStore(
        val dataStoreName: String,
        val visibility: DataStoreVisibility,
        val pollInterval: Duration,
        val retryInterval: Duration,
        val exponentialBackoff: Boolean,
        val maximumRetryInterval: Duration,
        val lockTimeout: Duration,
        override val stepBuilder: StepBuilder,
    ) : Entry {
        init {
            require(dataStoreName.length <= MAX_DATA_STORE_NAME)
            require(pollInterval in 1.seconds..300.seconds)
            require(retryInterval in 1.minutes..1.days)
            require(maximumRetryInterval in 10.minutes..1.days)
            require(lockTimeout in 1.minutes..5.hours)
        }
    }
}

private const val EXCEPTION_SUBPROCESS = "ExceptionSubProcess"

private fun Entry.toProcess(): Process {
    val sequenceFlows = stepBuilder.buildSequenceFlows("")

    return Process(
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
        callActivities = stepBuilder.callActivities,
        startEvent = StartEvent(
            id = "StartEvent",
            name = when (this) {
                is Entry.DataStore,
                is Entry.HttpsBuilder,
                -> requireNotNull(stepBuilder.startMessage) {
                    "You need to call startMessage() first"
                }.name

                is Entry.Timer.Scheduled -> "StartEvent"
            },
            extensionElements = when (this) {
                is Entry.HttpsBuilder,
                is Entry.DataStore,
                -> ExtensionElements(
                    properties = listOf(
                        Property("componentVersion", "1.0"),
                        Property(
                            "cmdVariantUri",
                            "ctype::FlowstepVariant/cname::MessageStartEvent/version::1.0",
                        ),
                    ),
                )

                is Entry.Timer -> null
            },
            timerEventDefinition = when (this) {
                is Entry.HttpsBuilder -> null

                is Entry.DataStore -> null

                is Entry.Timer.Scheduled -> StartEvent.TimerEventDefinition(
                    id = "TimerEventDefinition_0",
                    extensionElements = ExtensionElements(
                        listOf(
                            Property("componentVersion", "1.4"),
                            Property(
                                "cmdVariantUri",
                                "ctype::FlowstepVariant/cname::intermediatetimer/version::1.4.0",
                            ),
                            Property("activityType", "StartTimerEvent"),
                            Property(
                                "scheduleKey",
                                buildXml {
                                    row {
                                        cell("dateType")
                                        cell("ADVANCED")
                                    }
                                    row {
                                        cell("hourValue")
                                        cell("")
                                    }
                                    row {
                                        cell("minutesValue")
                                        cell("")
                                    }
                                    row {
                                        cell("timeType")
                                        cell("ON_TIME")
                                    }
                                    row {
                                        cell("timeZone")
                                        cell("( UTC 0:00 ) Greenwich Mean Time(Etc/GMT)")
                                    }
                                    row {
                                        cell("throwExceptionOnExpiry")
                                        cell("true")
                                    }
                                    row {
                                        cell("second")
                                        cell(second)
                                    }
                                    row {
                                        cell("minute")
                                        cell(minute)
                                    }
                                    row {
                                        cell("hour")
                                        cell(hour)
                                    }
                                    row {
                                        cell("day_of_month")
                                        cell(dayOfMonth)
                                    }
                                    row {
                                        cell("month")
                                        cell(month)
                                    }
                                    row {
                                        cell("dayOfWeek")
                                        cell(dayOfWeek)
                                    }
                                    row {
                                        cell("year")
                                        cell(year)
                                    }
                                    row {
                                        cell("startAt")
                                        cell("")
                                    }
                                    row {
                                        cell("endAt")
                                        cell("")
                                    }
                                    row {
                                        cell("attributeBehaviour")
                                        cell(
                                            "isThrowExceptionOnExpiryVisible,isScheduleAdvancedVisible,isScheduleAdvancedStartEndVisible,isScheduleSimpleVisible",
                                        )
                                    }
                                    row {
                                        cell("triggerType")
                                        cell("cron")
                                    }
                                    row {
                                        cell("noOfSchedules")
                                        cell("1")
                                    }
                                    row {
                                        cell("schedule1")
                                        cell(
                                            quartzCron.replace(
                                                " ",
                                                "+",
                                            ) + "&amp;trigger.timeZone=Etc/GMT",
                                        )
                                    }
                                },
                            ),
                        ),
                    ),
                )
            },
            messageEventDefinition = when (this) {
                is Entry.DataStore -> ""
                is Entry.HttpsBuilder -> ""
                is Entry.Timer.Scheduled -> null
            },
            outgoing = "SequenceFlow_Start",
        ),
        endEvent = stepBuilder.endMessage.toEvent("EndEvent", "SequenceFlow_End"),
        sequenceFlows = sequenceFlows,
        subProcess = if (stepBuilder.subprocess != null) {
            val subprocess = stepBuilder.subprocess!!

            val startMessage = requireNotNull(subprocess.startMessage) {
                "You need to call startErrorEvent() inside exceptionSubprocess"
            }
            require(startMessage is StepBuilder.ErrorStartMessage)
            val suffix = "_$EXCEPTION_SUBPROCESS"

            val sequenceFlows = subprocess.buildSequenceFlows(suffix)

            Process(
                id = "SubProcess",
                name = "Exception Subprocess",
                extensionElements = ExtensionElements(
                    listOf(
                        Property("componentVersion", "1.1"),
                        Property("activityType", "ErrorEventSubProcessTemplate"),
                        Property(
                            "cmdVariantUri",
                            "ctype::FlowstepVariant/cname::ErrorEventSubProcessTemplate/version::1.1.0",
                        ),
                    ),
                ),
                startEvent = StartEvent(
                    id = "StartEvent$suffix",
                    name = startMessage.name,
                    extensionElements = null,
                    timerEventDefinition = null,
                    outgoing = "SequenceFlow_Start$suffix",
                    errorEventDefinition = EndEvent.ErrorEventDefinition(
                        extensionElements = ExtensionElements(
                            listOf(
                                Property("cmdVariantUri", "ctype::FlowstepVariant/cname::ErrorStartEvent"),
                                Property("activityType", "StartErrorEvent"),
                            ),
                        ),
                    ),
                ),
                endEvent = subprocess.endMessage.toEvent(
                    "EndEvent$suffix",
                    incoming = if (subprocess.callActivities.isNotEmpty()) "SequenceFlow_End$suffix" else "SequenceFlow_Start$suffix",
                ),
                callActivities = subprocess.callActivities,
                sequenceFlows = sequenceFlows,
                subProcess = null,
            )
        } else {
            null
        },
    )
}

private const val MAX_DATA_STORE_NAME = 40

@FlowDSL
public sealed class StepBuilder private constructor(
    internal val callActivities: MutableList<CallActivity> = mutableListOf(),
    private val idPrefix: String = "",
    private val suffix: String,
) {
    internal sealed interface EndMessage {
        fun toEvent(
            id: String,
            incoming: String,
        ): EndEvent
    }

    protected data class NormalEndMessage(val name: String) : EndMessage {
        override fun toEvent(
            id: String,
            incoming: String,
        ): EndEvent = EndEvent(
            id = id,
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("componentVersion", "1.1"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::MessageEndEvent/version::1.1.0"),
                ),
            ),
            incoming = incoming,
        )
    }

    protected data class ErrorEndMessage(val name: String) : EndMessage {
        override fun toEvent(
            id: String,
            incoming: String,
        ): EndEvent = EndEvent(
            id = id,
            name = name,
            errorEventDefinition = EndEvent.ErrorEventDefinition(
                ExtensionElements(
                    listOf(
                        Property("cmdVariantUri", "ctype::FlowstepVariant/cname::ErrorEndEvent"),
                        Property("activityType", "EndErrorEvent"),
                    ),
                ),
            ),
            incoming = incoming,
            extensionElements = null,
            messageEventDefinition = null,
        )
    }

    internal sealed interface StartMessage {
        val name: String
    }

    internal data class NormalStartMessage(override val name: String) : StartMessage
    internal data class ErrorStartMessage(override val name: String) : StartMessage
    internal data object TimerStartMessage : StartMessage {
        override val name: String = "Start Event"
    }

    internal lateinit var startMessage: StartMessage
    internal lateinit var endMessage: EndMessage

    public class NeedsStartMessage internal constructor(suffix: String) : StepBuilder(suffix = suffix) {
        public fun startMessage(name: String = "Start") {
            startMessage = NormalStartMessage(name)
        }
    }

    internal class Nested(callActivities: MutableList<CallActivity>, prefix: String, suffix: String) :
        StepBuilder(callActivities, prefix, suffix)

    internal class Normal : StepBuilder(suffix = "")

    public fun withPrefix(prefix: String, action: StepBuilder.() -> Unit) {
        require(prefix.isNotBlank()) { "Prefix cannot be blank" }
        Nested(callActivities, prefix, suffix = suffix).action()
    }

    public fun endMessage(name: String = "End") {
        endMessage = NormalEndMessage(name)
    }

    internal var subprocess: Exception? = null

    public fun exceptionSubprocess(subprocess: Exception.() -> Unit) {
        this.subprocess = Exception().apply(subprocess)
    }

    public class Exception internal constructor() : StepBuilder(suffix = "_$EXCEPTION_SUBPROCESS") {
        public fun startErrorEvent(name: String = "Error Start") {
            startMessage = ErrorStartMessage(name)
        }

        public fun errorEndEvent(name: String = "Error End") {
            endMessage = ErrorEndMessage(name)
        }
    }

    public fun groovyScript(
        name: String = "Custom Groovy Script",
        function: String = "processData",
        file: String,
    ): CallActivity {
        val previous = callActivities.lastOrNull()
        if (previous != null) {
            callActivities[callActivities.lastIndex] = previous.copy(
                outgoing = "SequenceFlow_${callActivities.size - 1}$suffix",
            )
        }

        val groovyScript = CallActivity(
            id = "${idPrefix}GroovyScript_${callActivities.size}$suffix",
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("scriptFunction", function),
                    Property("scriptBundleId", ""),
                    Property("componentVersion", "1.1"),
                    Property("activityType", "Script"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::GroovyScript/version::1.1.2"),
                    Property("subActivityType", "GroovyScript"),
                    Property("script", file),
                ),
            ),
            incoming = if (previous ==
                null
            ) {
                "SequenceFlow_Start$suffix"
            } else {
                "SequenceFlow_${callActivities.size - 1}$suffix"
            },
            outgoing = "SequenceFlow_End$suffix",
        )

        callActivities.add(groovyScript)
        return groovyScript
    }

    public fun contentModifier(
        name: String = "Content Modifier",
        builder: ContentModifierBuilder.() -> Unit,
    ): CallActivity {
        val previous = callActivities.lastOrNull()
        if (previous != null) {
            callActivities[callActivities.lastIndex] = previous.copy(
                outgoing = "SequenceFlow_${callActivities.size - 1}$suffix",
            )
        }

        val contentModifierBuilder = ContentModifierBuilder().apply(builder)

        val contentModifier = CallActivity(
            id = "${idPrefix}ContentModifier_${callActivities.size}$suffix",
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("bodyType", contentModifierBuilder.bodyType.toString()),
                    Property("propertyTable", contentModifierBuilder.buildProperties()),
                    Property("headerTable", contentModifierBuilder.buildHeader()),
                    Property("wrapContent", contentModifierBuilder.body),
                    Property("componentVersion", "1.6"),
                    Property("activityType", "Enricher"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::Enricher/version::1.6.0"),
                ),
            ),
            incoming = if (previous ==
                null
            ) {
                "SequenceFlow_Start$suffix"
            } else {
                "SequenceFlow_${callActivities.size - 1}$suffix"
            },
            outgoing = "SequenceFlow_End$suffix",
        )

        callActivities.add(contentModifier)
        return contentModifier
    }

    /**
     * Store the whole message in the [dataStoreName] for a [expirationPeriod] with the [entryID].
     */
    public fun write(
        name: String = "Write",
        dataStoreName: String,
        visibility: DataStoreVisibility = DataStoreVisibility.Global,
        /**
         * If empty, it is set by the SapDataStoreId header, or will be generated and filled in this header
         */
        entryID: String? = null,
        retentionThreshold: Duration = 2.days,
        expirationPeriod: Duration = 30.days,
        encryptStoredMessage: Boolean = false,
        overwrite: Boolean = false,
        includeMessageHeaders: Boolean = false,
    ): CallActivity {
        require(expirationPeriod.inWholeDays > retentionThreshold.inWholeDays)
        require(expirationPeriod <= 180.days)
        require(dataStoreName.length <= MAX_DATA_STORE_NAME)

        val previous = callActivities.lastOrNull()
        if (previous != null) {
            callActivities[callActivities.lastIndex] = previous.copy(
                outgoing = "SequenceFlow_${callActivities.size - 1}$suffix",
            )
        }

        val write = CallActivity(
            id = "${idPrefix}Write${callActivities.size}$suffix",
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("storageName", dataStoreName),
                    Property("visibility", visibility.id),
                    Property("messageId", entryID ?: ""),
                    Property("alert", retentionThreshold.inWholeDays.toString()),
                    Property("expire", expirationPeriod.inWholeDays.toString()),
                    Property("encrypt", encryptStoredMessage.toString()),
                    Property("override", overwrite.toString()),
                    Property("includeMessageHeaders", includeMessageHeaders.toString()),
                    Property("componentVersion", "1.7"),
                    Property("activityType", "DBstorage"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::put/version::1.7.1"),
                    Property("operation", "put"),
                ),
            ),
            incoming = if (previous ==
                null
            ) {
                "SequenceFlow_Start$suffix"
            } else {
                "SequenceFlow_${callActivities.size - 1}$suffix"
            },
            outgoing = "SequenceFlow_End$suffix",
        )

        callActivities.add(write)
        return write
    }

    /**
     * Get the whole message in the [dataStoreName] with the [entryID].
     *
     * This step also sets the headers `SAP_DataStoreCreatedAt` and `SAP_DataStoreExpiresAt` representing a timestamp.
     */
    public fun get(
        name: String = "Get",
        dataStoreName: String,
        visibility: DataStoreVisibility = DataStoreVisibility.Global,
        /**
         * If empty, it is set by the SapDataStoreId header, or will be generated and filled in this header
         */
        entryID: String? = null,
        deleteOnCompletion: Boolean = false,
        /**
         * If you disable this option, the header `SAP_DatastoreEntryFound` is set to false and no exception is thrown, even if the [entryID] does not exist.
         */
        throwExceptionOnMissingEntry: Boolean = true,
    ): CallActivity {
        require(dataStoreName.length <= MAX_DATA_STORE_NAME)

        val previous = callActivities.lastOrNull()
        if (previous != null) {
            callActivities[callActivities.lastIndex] = previous.copy(
                outgoing = "SequenceFlow_${callActivities.size - 1}$suffix",
            )
        }

        val get = CallActivity(
            id = "${idPrefix}Get${callActivities.size}$suffix",
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("storageName", dataStoreName),
                    Property("visibility", visibility.id),
                    Property("dataStoreId", entryID ?: ""),
                    Property("delete", deleteOnCompletion.toString()),
                    Property("stopOnMissingEntry", throwExceptionOnMissingEntry.toString()),
                    Property("componentVersion", "1.7"),
                    Property("activityType", "DBstorage"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::get/version::1.7.1"),
                    Property("operation", "get"),
                ),
            ),
            incoming = if (previous ==
                null
            ) {
                "SequenceFlow_Start$suffix"
            } else {
                "SequenceFlow_${callActivities.size - 1}$suffix"
            },
            outgoing = "SequenceFlow_End$suffix",
        )

        callActivities.add(get)
        return get
    }

    /**
     * Get all messages in the [dataStoreName] as XML.
     */
    public fun select(
        name: String = "Select",
        dataStoreName: String,
        visibility: DataStoreVisibility = DataStoreVisibility.Global,
        /**
         * Or via the header `SapDataStoreMaxResults`
         */
        numberOfPolledMessages: Int = 1,
        deleteOnCompletion: Boolean = false,
    ): CallActivity {
        require(dataStoreName.length <= MAX_DATA_STORE_NAME)

        val previous = callActivities.lastOrNull()
        if (previous != null) {
            callActivities[callActivities.lastIndex] = previous.copy(
                outgoing = "SequenceFlow_${callActivities.size - 1}$suffix",
            )
        }

        val get = CallActivity(
            id = "${idPrefix}Select${callActivities.size}$suffix",
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("storageName", dataStoreName),
                    Property("visibility", visibility.id),
                    Property("maxresults", numberOfPolledMessages.toString()),
                    Property("delete", deleteOnCompletion.toString()),
                    Property("componentVersion", "1.7"),
                    Property("activityType", "DBstorage"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::select/version::1.7.1"),
                    Property("operation", "select"),
                ),
            ),
            incoming = if (previous ==
                null
            ) {
                "SequenceFlow_Start$suffix"
            } else {
                "SequenceFlow_${callActivities.size - 1}$suffix"
            },
            outgoing = "SequenceFlow_End$suffix",
        )

        callActivities.add(get)
        return get
    }

    /**
     * Delete messages in the [dataStoreName].
     */
    public fun delete(
        name: String = "Delete",
        dataStoreName: String,
        visibility: DataStoreVisibility = DataStoreVisibility.Global,
        /**
         * If empty, it is set by the SapDataStoreId header, or will delete all messages.
         */
        entryID: String? = null,
    ): CallActivity {
        require(dataStoreName.length <= MAX_DATA_STORE_NAME)

        val previous = callActivities.lastOrNull()
        if (previous != null) {
            callActivities[callActivities.lastIndex] = previous.copy(
                outgoing = "SequenceFlow_${callActivities.size - 1}$suffix",
            )
        }

        val delete = CallActivity(
            id = "${idPrefix}Delete${callActivities.size}$suffix",
            name = name,
            extensionElements = ExtensionElements(
                listOf(
                    Property("storageName", dataStoreName),
                    Property("visibility", visibility.id),
                    Property("messageId", entryID ?: ""),
                    Property("componentVersion", "1.7"),
                    Property("activityType", "DBstorage"),
                    Property("cmdVariantUri", "ctype::FlowstepVariant/cname::delete/version::1.7.1"),
                    Property("operation", "delete"),
                ),
            ),
            incoming = if (previous ==
                null
            ) {
                "SequenceFlow_Start$suffix"
            } else {
                "SequenceFlow_${callActivities.size - 1}$suffix"
            },
            outgoing = "SequenceFlow_End$suffix",
        )

        callActivities.add(delete)
        return delete
    }
}

public enum class DataStoreVisibility(internal val id: String) {
    Global("global"),
    IntegrationFlow("local"),
}

@FlowDSL
public class ContentModifierBuilder internal constructor() {
    private val constant = mutableMapOf<String, String>()
    private val externalParameters = mutableSetOf<String>()
    private val properties = mutableMapOf<String, String>()
    private val addHeaders = mutableMapOf<String, Pair<String, HeaderPropertyType>>()
    private val deleteHeaders = mutableSetOf<String>()

    internal var body = ""
    internal var bodyType = BodyType.Expression

    public fun setBody(value: String, type: BodyType = BodyType.Constant) {
        body = value
        bodyType = type
    }

    public fun constant(
        name: String,
        value: String,
    ) {
        constant[name] = value
    }

    public fun externalParameter(
        name: String,
    ) {
        externalParameters.add(name)
    }

    public fun property(
        name: String,
        propertyName: String,
    ) {
        properties[name] = propertyName
    }

    public enum class BodyType {
        Constant {
            override fun toString(): String = "constant"
        },
        Expression {
            override fun toString(): String = "expression"
        },
    }

    public enum class HeaderPropertyType {
        Constant {
            override fun toString(): String = "constant"
        },
        Expression {
            override fun toString(): String = "expression"
        },
        GlobalVariable {
            override fun toString(): String = "global persisted variables"
        },
        Header {
            override fun toString(): String = "header"
        },
        LocalVariable {
            override fun toString(): String = "persisted variables"
        },
        NumberRange {
            override fun toString(): String = "numberRange"
        },
        Property {
            override fun toString(): String = "property"
        },
        XPath {
            override fun toString(): String = "xpath"
        },
    }

    public fun addHeader(
        name: String,
        value: String,
        type: HeaderPropertyType = HeaderPropertyType.Constant,
    ) {
        addHeaders[name] = value to type
    }

    public fun deleteHeader(
        name: String,
    ) {
        deleteHeaders.add(name)
    }

    internal fun buildProperties(): String = buildXml {
        for ((name, value) in constant) {
            row {
                cell(id = "Action", value = "Create")
                cell(id = "Type", value = "constant")
                cell(id = "Value", value = value)
                cell(id = "Default", value = "")
                cell(id = "Name", value = name)
                cell(id = "Datatype", value = "")
            }
        }
        for (name in externalParameters) {
            row {
                cell(id = "Action", value = "Create")
                cell(id = "Type", value = "constant")
                cell(id = "Value", value = "{{$name}}")
                cell(id = "Default", value = "")
                cell(id = "Name", value = name)
                cell(id = "Datatype", value = "")
            }
        }
        for ((name, propertyName) in properties) {
            row {
                cell(id = "Action", value = "Create")
                cell(id = "Type", value = "property")
                cell(id = "Value", value = propertyName)
                cell(id = "Default", value = "")
                cell(id = "Name", value = name)
                cell(id = "Datatype", value = "")
            }
        }
    }

    internal fun buildHeader(): String = buildXml {
        for ((name, value) in addHeaders) {
            row {
                cell(id = "Action", value = "Create")
                cell(id = "Type", value = value.second.toString())
                cell(id = "Value", value = value.first)
                cell(id = "Default", value = "")
                cell(id = "Name", value = name)
                cell(id = "Datatype", value = "")
            }
        }

        for (name in deleteHeaders) {
            row {
                cell(id = "Action", value = "Delete")
                cell(id = "Type", value = HeaderPropertyType.Constant.toString())
                cell(id = "Value", value = "")
                cell(id = "Default", value = "")
                cell(id = "Name", value = $$"$name=$$name")
                cell(id = "Datatype", value = "")
            }
        }
    }
}

@JvmInline
public value class MB(internal val value: Int)

public inline val Int.MB: MB get() = MB(this)
