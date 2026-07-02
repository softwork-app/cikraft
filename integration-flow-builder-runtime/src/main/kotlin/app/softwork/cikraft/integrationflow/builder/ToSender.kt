package app.softwork.cikraft.integrationflow.builder

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.integrationflow.Config
import app.softwork.cikraft.integrationflow.MessageFlow
import kotlin.time.Duration.Companion.seconds

internal fun MessageFlow.toSender(config: Config): CreatedFlow.Sender = when (name) {
    "HTTPS" -> CreatedFlow.Sender.Https(
        url = extensionElements.properties.single { it.key == "urlPath" }.value.removePrefix(
            config.baseUrl,
        ),
        role = extensionElements.properties.single { it.key == "userRole" }.value,
        csrfProtection = extensionElements.properties.single { it.key == "xsrfProtection" }.value.toInt() == 1,
    )

    "DataStore" -> CreatedFlow.Sender.DataStore(
        name = extensionElements.properties.single { it.key == "storageName" }.value,
        pollDelay = (extensionElements.properties.single { it.key == "PollDelay" }.value).toInt().seconds,
    )

    else -> error("Not yet supported")
}
