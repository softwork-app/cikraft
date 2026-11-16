package app.softwork.cikraft.integrationflow.builder

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.integrationflow.Config
import app.softwork.cikraft.integrationflow.MessageFlow
import app.softwork.cikraft.integrationflow.StartEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlValue
import kotlin.time.Duration.Companion.seconds

internal fun MessageFlow.toSender(config: Config): CreatedFlow.Type = when (name) {
    "HTTPS" -> CreatedFlow.Type.Https(
        url = extensionElements.properties.single { it.key == "urlPath" }.value.removePrefix(
            config.baseUrl,
        ),
        role = extensionElements.properties.single { it.key == "userRole" }.value,
        csrfProtection = extensionElements.properties.single { it.key == "xsrfProtection" }.value.toInt() == 1,
    )

    "DataStore" -> CreatedFlow.Type.DataStore(
        name = extensionElements.properties.single { it.key == "storageName" }.value,
        pollDelay = (extensionElements.properties.single { it.key == "PollDelay" }.value).toInt().seconds,
    )

    else -> error("Not yet supported $name")
}

internal fun StartEvent.TimerEventDefinition.toSender(): CreatedFlow.Type.Scheduler {
    val schedulerValue = extensionElements.properties.single { it.key == "scheduleKey" }.value
    val fixedXml = "<f>$schedulerValue</f>"
    val quartzXml = XML.v1.decodeFromString(XmlListHolder.serializer(), fixedXml).rows

    val quartzCron = quartzXml.single {
        it.cells.first().value == "schedule1"
    }.cells.last().value.split("&").first().replace("+", " ")

    return CreatedFlow.Type.Scheduler(
        quartzCron = quartzCron,
    )
}

@Serializable
@SerialName("f")
private data class XmlListHolder(val rows: List<Row>)

@Serializable
@SerialName("row")
private data class Row(val cells: List<Cell>)

@Serializable
@SerialName("cell")
private data class Cell(val id: String? = null, @XmlValue val value: String)
