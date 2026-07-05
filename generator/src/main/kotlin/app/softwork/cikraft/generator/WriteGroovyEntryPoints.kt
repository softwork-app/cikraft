package app.softwork.cikraft.generator

import app.softwork.cikraft.core.*

public fun writeGroovyEntryPoints(
    scripts: List<Script>,
): String = buildString {
    appendLine("import com.sap.gateway.ip.core.customdev.util.Message")
    appendLine()

    for (script in scripts) {
        appendLine("Message ${script.name}(Message message) {")
        appendLine(
            "  return CiKraftEntrypointsKt.${script.name}(message, messageLogFactory.getMessageLog(message))",
        )
        appendLine("}")
    }
}
